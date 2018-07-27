package io.gitlab.leibnizhu.vertXearch

import java.io.File

import com.hankcs.lucene.HanLPAnalyzer
import io.vertx.core.json.JsonObject
import io.vertx.scala.core.{Context, Vertx}

object Constants {
  /**
    * Vert.X变量
    */
  var vertx: Vertx = _
  private var vertxContext: Context = _
  private var config: JsonObject = _

  /**
    * 从配置文件读取到的配置
    */
  var indexPath: String = _ //索引文件目录
  var articlePath: String = _ //文章目录
  var timestampFile: String = _ //保存索引的时间戳的文件路径
  var refreshTimerInterval: Long = _ //更新索引的时间建个
  var keywordPreTag: String = _ //返回的关键词前置标签,这里弄成红色字体
  var keywordPostTag: String = _ //返回的关键词后置标签,要和前置标签闭合

  /**
    * Lucene文档(Document)中的key,基本不用动
    */
  val ID: String = "id" //文章ID key
  val TITLE: String = "title" //文章标题key
  val AUTHOR: String = "author" //文章作者key
  val CONTENTS: String = "contents" //文章内容key

  /**
    * 模块的一些配置, 也基本不需要动
    */
  val MAX_SEARCH: Int = 30 //单次搜索默认最大返回文章个数
  val FRAGMENT_SIZE: Int = 150 //片段最大长度
  val MAX_HIGHLIGHTER: Int = 10 //最多的高亮次数
  val LINE_SEPARATOR: String = System.getProperty("line.separator", "\n") //路径分隔符,从系统配置读取
  val ANALYZER: HanLPAnalyzer = new HanLPAnalyzer //分词器,目前用HanLP

  def init(ctx:Context):Unit ={
    this.vertxContext = ctx
    this.vertx = ctx.owner
    this.config = vertxContext.config().get
    this.indexPath = config.getString("indexPath", "indexes")
    this.articlePath = config.getString("articlePath", "articles")
    //确保路径以/结尾
    if (!this.indexPath.endsWith(File.separator)) this.indexPath += File.separator
    if (!this.articlePath.endsWith(File.separator)) this.articlePath += File.separator
    this.timestampFile = s"${this.indexPath}index.ts"
    this.refreshTimerInterval = this.config.getInteger("refreshIndexPerSecond", 300) * 1000L //默认5分钟
    this.keywordPreTag = this.config.getString("keywordPreTag", "<font color='red'>") //默认弄成红色字体
    this.keywordPostTag = this.config.getString("keywordPostTag", "</font>")
  }
}
