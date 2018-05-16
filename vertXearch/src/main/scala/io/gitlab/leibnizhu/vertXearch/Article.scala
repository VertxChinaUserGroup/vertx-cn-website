package io.gitlab.leibnizhu.vertXearch

import java.io.File

import io.gitlab.leibnizhu.vertXearch.Constants.{LINE_SEPARATOR, vertx}
import io.vertx.core.buffer.Buffer
import io.vertx.core.{AsyncResult, Future, Handler}
import org.slf4j.LoggerFactory

case class Article(id: String, title: String, author: String, content: String) {
  /**
    * 将Article对象写入到文件,目录由配置文件指定
    *
    * @param callback 写入到文件之后的回调,无传入结果
    */
  def writeToFile(callback: Handler[AsyncResult[Unit]]): Unit = {
    val fileName = Constants.articlePath() + "/" + this.id + ".txt"
    val fileContent = this.title + LINE_SEPARATOR + this.author + LINE_SEPARATOR + this.content
    Constants.vertx.fileSystem().writeFile(fileName, Buffer.buffer(fileContent), callback)
  }
}

object Article {
  private val log = LoggerFactory.getLogger(Article.getClass)

  /**
    * 从文件读取文章
    *
    * @param file 文章txt文件
    * @param handler 读取文章之后的回调,传入解析到的Article
    */
  def fromFile(file: File, handler: Handler[AsyncResult[Article]]): Unit = {
    vertx.fileSystem().readFile(file.getAbsolutePath, res => {
      if (res.succeeded()) {
        val article = parse(file, res.result())
        log.info(s"读取文章文件${file.getName}成功")
        handler.handle(Future.succeededFuture(article))
      } else {
        log.error("读取文章文件失败.", res.cause())
        handler.handle(Future.failedFuture(res.cause()))
      }
    })
  }

  /**
    * 解析文章
    * 文件名:[ID].txt
    * 第一行标题，第二行作者，第三行开始正文
    *
    * @param file 文件对象
    * @param buffer 读取到文件内容的Buffer
    * @return
    */
  def parse(file: File, buffer: Buffer): Article = {
    val filename = file.getName
    val id = filename.substring(0, filename.lastIndexOf('.'))
    val fileContent = buffer.toString()
    val fistLineIndex = fileContent.indexOf(LINE_SEPARATOR)
    val title = fileContent.substring(0, fistLineIndex)
    val secondLineIndex = fileContent.indexOf(LINE_SEPARATOR, fistLineIndex + LINE_SEPARATOR.length)
    val author = fileContent.substring(fistLineIndex + LINE_SEPARATOR.length, secondLineIndex)
    val content = fileContent.substring(secondLineIndex + LINE_SEPARATOR.length)
    //HanLp区分大小写，所以全转小写
    Article(id, title.toLowerCase, author.toLowerCase, content.toLowerCase)
  }
}