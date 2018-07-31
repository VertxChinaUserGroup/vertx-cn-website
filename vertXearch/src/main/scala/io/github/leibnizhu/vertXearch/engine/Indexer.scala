package io.github.leibnizhu.vertXearch.engine

import java.io.File
import java.nio.file.Paths

import io.github.leibnizhu.vertXearch.utils.Article
import io.github.leibnizhu.vertXearch.utils.Constants._
import io.vertx.scala.core.{CompositeFuture, Future}
import org.apache.lucene.document._
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.store.FSDirectory
import org.slf4j.LoggerFactory

class Indexer(indexDirectoryPath: String) {
  private val log = LoggerFactory.getLogger(getClass)
  private val indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath))
  val writer: IndexWriter = new IndexWriter(indexDirectory, new IndexWriterConfig(ANALYZER))

  /**
    * 关闭索引Writer
    */
  def close(): Unit =
    writer.close()

  /**
    * 删除所有索引,慎用
    */
  def cleanAllIndex(): Unit =
    if (writer.isOpen) {
      writer.deleteAll()
      writer.commit()
    }

  def deleteDocument(id: String): Long =
    writer.deleteDocuments(new Term(ID, id))

  /**
    * 读取指定目录下的所有文章,建立索引
    *
    * @param dataDirPath 存储文章txt文件的目录
    * @param callback    建立索引成功之后的回调,传入产生的索引数量
    */
  def createIndex(dataDirPath: String, callback: Future[Int]): Unit =
    createIndex(new File(dataDirPath).listFiles
      .filter(file => !file.isDirectory && file.exists && file.canRead && file.getName.endsWith(".txt")),
      callback)

  /**
    * 读取指定目录下的所有文章,建立索引
    *
    * @param files    需要加入索引的文章txt文件
    * @param callback 建立索引成功之后的回调,传入产生的索引数量
    */
  def createIndex(files: Array[File], callback: Future[Int]): Unit =
    CompositeFuture.all(files.map(indexFile(_, Future.future[Boolean]())).toBuffer)
      .setHandler(ar =>
        if (ar.succeeded()) {
          writer.commit()
          callback.complete(writer.numDocs)
        } else {
          callback.fail(ar.cause())
        })

  /**
    * 添加单个文件索引到Writer
    *
    * @param file     文章文件
    * @param callback 加入writer之后的回调,传入是否成功
    */
  private def indexFile(file: File, callback: Future[Boolean]): Future[Boolean] = {
    readDocument(file, Future.future[Document]().setHandler(ar => {
      if (ar.succeeded()) {
        val doc = ar.result()
        //创建前尝试先删除已有的 writer.deleteDocuments(new Term(ID, doc.get(ID)))
        writer.updateDocument(new Term(ID, doc.get(ID)), doc)
        callback.complete(true)
      } else {
        callback.fail(ar.cause())
      }
    }))
    callback
  }

  /**
    * 读取单个文件并创建Document
    * 处理文件ID\标题\作者\内容,而作者不进行分词
    * ID还是要加入索引的,否则更新文件内容的时候,不能根据ID查出旧文档进行更新(上面indexFile()方法)
    *
    * @param file     文章文件
    * @param callback 创建Document之后的回调,传入Document
    */
  private def readDocument(file: File, callback: Future[Document]): Unit = {
    Article.fromFile(file, Future.future[Article]().setHandler(ar => {
      if (ar.succeeded()) {
        val article = ar.result()
        log.info(s"读取到文章(ID=${article.id})")
        val document = new Document
        document.add(new StringField(ID, article.id, Field.Store.YES))
//        document.add(new TextField(TITLE, article.title, Field.Store.YES))
//        document.add(new Field(AUTHOR, article.author, FieldTypeFactory.storedNotAnalyzed)) //作者不需要分词
        document.add(new TextField(CONTENTS, article.content.toString, Field.Store.YES))
        callback.complete(document)
      } else {
        log.error("读取文章文件失败.", ar.cause())
        callback.fail(ar.cause())
      }
    }))
  }
}
