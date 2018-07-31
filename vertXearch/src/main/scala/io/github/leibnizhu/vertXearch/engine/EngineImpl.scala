package io.github.leibnizhu.vertXearch.engine

import java.io.File

import io.github.leibnizhu.vertXearch.utils.Article
import io.github.leibnizhu.vertXearch.utils.Constants._
import io.vertx.core.buffer.Buffer
import io.vertx.scala.core.Future
import org.apache.lucene.search.highlight._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.Try


class EngineImpl(indexPath: String, articlePath: String) extends Engine {
  private val log: Logger = LoggerFactory.getLogger(getClass)
  private val indexer: Indexer = new Indexer(indexPath)
  private var searcher: Searcher = _
  private val formatter: Formatter = new SimpleHTMLFormatter(keywordPreTag, keywordPostTag)
  private val fragmenter: Fragmenter = new SimpleFragmenter(150)

  /**
    * 初始化完毕后建立索引,然后初始化Searcher
    * 以保证Searcher可以顺利初始化
    */
  def init(afterInit: Future[Unit]): Engine = {
    prepareDictionaries()
    val lostIndex = getLastIndexTimestamp == 0
    val future: Future[Int] = Future.future() //建立索引的Future
    future.setHandler(_ => {
      if (lostIndex) setCurrentIndexTimestamp(System.currentTimeMillis())
      this.searcher = new Searcher(indexPath)
      log.info("搜索引擎EngineImpl启动完毕")
      afterInit.complete()
    })
    if (lostIndex) {
      log.info("索引文件疑似丢失,准备重建索引...")
      indexer.createIndex(articlePath, future)
    } else {
      log.info("索引文件存在,无需重建...")
      future.complete(0)
    }
    this
  }

  /**
    * 准备文章目录和索引目录,没有则创建
    */
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
    future.setHandler(ar => if (ar.succeeded()) log.info("创建索引成功") else log.error("创建索引失败", ar.cause()))
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
    //从文件读取上次刷新的时间戳
    val lastRefreshTime = getLastIndexTimestamp
    val currentTime = System.currentTimeMillis()
    //打文章目录
    val files = new File(articlePath).listFiles()
    if (files != null && files.nonEmpty) {
      //找出文章目录里面,存在的,可读的,修改时间大于上次刷新时间戳的txt文件,即需要更新索引的文件
      val updatedFiles = files.filter(file => !file.isDirectory && file.exists && file.canRead && file.getName.endsWith(".txt") && file.lastModified() - lastRefreshTime >= 0)
      if (updatedFiles.length > 0) {
        val future: Future[Int] = Future.future()
        future.setHandler(ar => {
          if (ar.succeeded()) log.info("创建索引成功") else log.error("创建索引失败", ar.cause())
          searcher.refreshIndexSearcher()
        })
        indexer.createIndex(updatedFiles, future) //过滤后的文件进行创建/更新索引
      } else {
        log.info("没有更新了的文章")
      }
    }
    //将本次更新的时间戳写入到文件
    setCurrentIndexTimestamp(currentTime)
  }

  private def setCurrentIndexTimestamp(currentTime: Long) = vertx.fileSystem().writeFileBlocking(timestampFile, Buffer.buffer(currentTime.toString))

  private def getLastIndexTimestamp = Try(vertx.fileSystem().readFileBlocking(timestampFile).toString().toLong).getOrElse(0L)

  /**
    * 清理文章文件已经被删除,但是索引里还有的那些Document
    * 查出所有存活文章,过滤出文件已删除的,再从索引中删除
    */
  def cleanDeletedArticles(): Unit = {
    val deleted = searcher.getAllDocuments //所有存活的文档
      .filter(doc => !vertx.fileSystem().existsBlocking(articlePath + doc.get(ID) + ".txt")) //过滤出文章文件不存在的
      .map(doc => {
        log.info(s"发现文档(ID=${doc.get(ID)})在文章目录中已被删除,准备从索引中同步删除...")
        indexer.deleteDocument(doc.get(ID))
      }).size
    if (deleted > 0) {
      //有删除的文档索引,需要提交,同时刷新searcher
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
  override def search(searchQuery: String, length: Int, callback: Future[List[Article]]): Unit = {
    handleTryWithFuture(Try({
      val (query, docs) = searcher.search(searchQuery.toLowerCase(), length)
      //设置高亮格式//设置高亮格式
      val highlighter = new Highlighter(formatter, new QueryScorer(query))
      //设置返回字符串长度
      highlighter.setTextFragmenter(fragmenter)
      docs.map(doc => {
        //FIXME 网上说这里的.replaceAll("\\s*", "")是必须的，\r\n这样的空白字符会导致高亮标签错位,但实测好像并不影响
        //        val title = doc.get(TITLE)//.replaceAll("\\s*", "")
        //        val author = doc.get(AUTHOR)//.replaceAll("\\s*", "")
        val content = doc.get(CONTENTS) //.replaceAll("\\s*", "")
        Article(doc.get(ID), //以下三个都是尝试高亮,高亮失败则用原来的纯文本
          //          Option(highlighter.getBestFragment(ANALYZER, TITLE, title)).getOrElse(title),
          //          Option(highlighter.getBestFragment(ANALYZER, AUTHOR, author)).getOrElse(author),
          Option(highlighter.getBestFragment(ANALYZER, CONTENTS, content)).getOrElse(subContext(content)))
      })
    }), callback)
  }

  /**
    * 截取片段长度
    *
    * @param content 截取的长度
    * @return
    */
  private def subContext(content: String) =
    if (content.length > FRAGMENT_SIZE) content.substring(0, FRAGMENT_SIZE) else content

  /**
    * 关闭搜索引擎
    */
  override def stop(callback: Future[Unit]): Unit =
    handleTryWithFuture(Try({
      indexer.close()
      searcher.close()
    }), callback)


  def handleTryWithFuture[T](tryObj: Try[T], callback: Future[T]): Unit =
    if (tryObj.isSuccess)
      callback.complete(tryObj.get)
    else
      callback.fail(tryObj.failed.get)
}
