package io.github.leibnizhu.vertXearch.utils

import java.io.File

import Constants.{LINE_SEPARATOR, vertx}
import io.vertx.core.buffer.Buffer
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.Future
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success, Try}

case class Article(id: String, content: String) extends Serializable {
  /**
    * 将Article对象写入到文件,目录由配置文件指定
    *
    * @param callback 写入到文件之后的回调,无传入结果
    */
  def writeToFile(callback: Try[Unit] => Unit): Unit = {
    val fileName = Constants.articlePath + "/" + this.id + ".txt"
    val fileContent = this.content
    Constants.vertx.fileSystem().writeFileFuture(fileName, Buffer.buffer(fileContent)).onComplete(callback)
  }

  def toJsonObject: JsonObject = new JsonObject()
      .put("id", this.id).put("content", this.content)

  def toLowerCase: Article = Article(this.id.toLowerCase, this.content.toLowerCase)
}

object Article {
  private val log = LoggerFactory.getLogger(Article.getClass)

  /**
    * 从文件读取文章
    *
    * @param file    文章txt文件
    * @param handler 读取文章之后的回调,传入解析到的Article
    */
  def fromFile(file: File, handler: Future[Article]): Unit =
    vertx.fileSystem().readFileFuture(file.getAbsolutePath).onComplete {
      case Success(result) =>
        log.info(s"读取文章文件${file.getName}成功")
        handler.complete(Article(file, result))
      case Failure(cause) =>
        log.error("读取文章文件失败.", cause)
        handler.fail(cause)
    }

  /**
    * 解析文章
    * 文件名:[ID].txt
    * 第一行标题，第二行作者，第三行开始正文
    *
    * @param file   文件对象
    * @param buffer 读取到文件内容的Buffer
    * @return
    */
  def apply(file: File, buffer: Buffer): Article = {
    val filename = file.getName
    val id = filename.substring(0, filename.lastIndexOf('.'))
    val fileContent = buffer.toString() //2018.07.30 提高通用性,不拆分文件内容了,直接做索引
//    val fistLineIndex = fileContent.indexOf(LINE_SEPARATOR)
//    val title = fileContent.substring(0, fistLineIndex)
//    val secondLineIndex = fileContent.indexOf(LINE_SEPARATOR, fistLineIndex + LINE_SEPARATOR.length)
//    val author = fileContent.substring(fistLineIndex + LINE_SEPARATOR.length, secondLineIndex)
//    val content = fileContent.substring(secondLineIndex + LINE_SEPARATOR.length)
    //HanLp区分大小写，所以全转小写
    Article(id, fileContent.toLowerCase)
  }
}