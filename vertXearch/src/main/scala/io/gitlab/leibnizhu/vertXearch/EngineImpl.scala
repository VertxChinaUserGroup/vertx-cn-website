package io.gitlab.leibnizhu.vertXearch


import java.io.File

import io.gitlab.leibnizhu.vertXearch.Constants._
import io.vertx.core.buffer.Buffer
import io.vertx.core.{AsyncResult, Future, Handler}
import org.apache.lucene.search.highlight._
import org.slf4j.LoggerFactory

import scala.util.Try


class EngineImpl(indexPath: String, articlePath: String, afterInit: Future[Unit]) extends Engine {
  private val log = LoggerFactory.getLogger(getClass)
  private val indexer: Indexer = new Indexer(indexPath)
  private var searcher: Searcher = _
  private val formatter: Formatter =new SimpleHTMLFormatter("<font color='red'>", "</font>")
  private val fragmenter: Fragmenter =new SimpleFragmenter(150)

  prepareFirstRunFuture()

  def prepareFirstRunFuture(): Unit = {
    val future: Future[Int] = Future.future()
    future.setHandler(_ => {
      this.searcher = new Searcher(indexPath)
      afterInit.complete()
    })
    prepareDictionaries()
    indexer.createIndex(articlePath, future)
  }

  private def prepareDictionaries(): Unit = {
    val dataDir = new File(articlePath)
    if (!dataDir.exists()) dataDir.mkdir()
    val indexDir = new File(indexPath)
    if (!indexDir.exists()) indexDir.mkdir()
  }

  /**
    * 对源目录下所有可用文件进行索引构建
    *
    * @return 增加索引的文件数量
    */
  override def createIndex(): Unit = {
    val future: Future[Int] = Future.future()
    future.setHandler(ar => {
      if (ar.succeeded()) {
        log.info("创建索引成功")
      } else {
        log.error("创建索引失败", ar.cause())
      }
    })
    indexer.createIndex(articlePath, future)
  }

  /**
    * 删除所有索引
    */
  override def cleanAllIndex(): Unit = indexer.cleanAllIndex()

  /**
    * 对源目录下所有可用文件进行索引更新
    *
    * @return 更新索引的文件数量
    */
  override def refreshIndex(): Unit = {
    val lastRefreshTime = Try(vertx.fileSystem().readFileBlocking(timestampFile()).toString().toLong).getOrElse(0L)
    val currentTime = System.currentTimeMillis()
    val files = new File(articlePath).listFiles()
    if (files != null && files.nonEmpty) {
      val updatedFiles = files.filter(file => !file.isDirectory && file.exists && file.canRead && file.getName.endsWith(".txt") && file.lastModified() - lastRefreshTime >= 0)
      if (updatedFiles.length > 0) {
        val future: Future[Int] = Future.future()
        future.setHandler(ar => {
          if (ar.succeeded()) {
            log.info("创建索引成功")
          } else {
            log.error("创建索引失败", ar.cause())
          }
          searcher.refreshIndexSearcher()
        })
        indexer.createIndex(updatedFiles, future)
      } else {
        log.info("没有更新了的文章")
      }
    }
    //将本次更新的时间戳写入到文件
    vertx.fileSystem().writeFileBlocking(timestampFile(), Buffer.buffer(currentTime.toString))
  }

  /**
    * 清理文章文件已经被删除,但是索引里还有的那些Document
    * 查出所有存活文章,过滤出文件已删除的,再从索引中删除
    */
  def cleanDeletedArticles(): Unit = {
    val deleted = searcher.getAllDocuments //所有存活的文档
      .filter(doc => !vertx.fileSystem().existsBlocking(articlePath + doc.get(ID) + ".txt")) //过滤出文章文件不存在的
      .map(doc => {
        log.info(s"发现文档(ID=${doc.get(ID)},标题=${doc.get(TITLE)})在文章目录中已被删除,准备从索引中同步删除...")
        indexer.deleteDocument(doc.get(ID))
      }).size
    if (deleted > 0) {
      indexer.writer.commit()
      searcher.refreshIndexSearcher()
    } else {
      log.info("没有被删除的文章")
    }
  }

  /**
    * 启动文章更新定时器
    *
    * @param interval 定时间隔
    */
  override def startRefreshTimer(interval: Long): Unit = {
    vertx.setPeriodic(interval, id => {
      log.info(s"开始定时更新索引,定时器ID=$id")
      this.refreshIndex()
    })
    vertx.setPeriodic(interval, id => {
      log.info(s"开始定时清理已删除文章的索引,定时器ID=$id")
      this.cleanDeletedArticles()
    })
  }

  /**
    * 按指定关键词进行查找
    *
    * @param searchQuery 查找关键词
    * @return 匹配的文档,按相关度降序
    */
  override def search(searchQuery: String, length: Int, callback: Handler[AsyncResult[List[Article]]]): Unit = {
    val trySearch = Try({
      val (query, docs) = searcher.search(searchQuery.toLowerCase(), length)
      //设置高亮格式//设置高亮格式
      val highlighter = new Highlighter(formatter, new QueryScorer(query))
      //设置返回字符串长度
      highlighter.setTextFragmenter(fragmenter)
      docs.map(doc => {
        //这里的.replaceAll("\\s*", "")是必须的，\r\n这样的空白字符会导致高亮标签错位
        val id = doc.get(ID)
        val content = doc.get(CONTENTS).replaceAll("\\s*", "")
        val highContext = highlighter.getBestFragment(analyzer, CONTENTS, content)
        val title = doc.get(TITLE).replaceAll("\\s*", "")
        val highTitle = highlighter.getBestFragment(analyzer, TITLE, title)
        val author = doc.get(AUTHOR).replaceAll("\\s*", "")
        val highAuthor = highlighter.getBestFragment(analyzer, AUTHOR, author)
        Article(id,
          Option(highTitle).getOrElse(title),
          Option(highAuthor).getOrElse(author),
          Option(highContext).getOrElse(subContext(content)))
      })
    })
    if(trySearch.isSuccess){
      callback.handle(Future.succeededFuture(trySearch.get))
    } else {
      callback.handle(Future.failedFuture(trySearch.failed.get))
    }
  }

  /**
    * 截取片段长度
    *
    * @param content
    * @return
    */
  private def subContext(content: String) =
    if (content.length > FRAGMENT_SIZE) content.substring(0, FRAGMENT_SIZE) else content

  /**
    * 关闭搜索引擎
    */
  override def stop(callback: Handler[AsyncResult[Unit]]): Unit = {
    val tryStop = Try({
      indexer.close()
      searcher.close()
    })
    callback.handle(if (tryStop.isSuccess) Future.succeededFuture() else Future.failedFuture(tryStop.failed.get))
  }
}
