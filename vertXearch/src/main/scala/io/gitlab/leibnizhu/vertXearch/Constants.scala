package io.gitlab.leibnizhu.vertXearch

import java.io.File

import com.hankcs.lucene.HanLPAnalyzer
import io.vertx.scala.core.{Context, Vertx}

object Constants {
  var vertx: Vertx = _
  var vertxContext: Context = _

  def init(ctx:Context):Unit ={
    this.vertxContext = ctx
    this.vertx = ctx.owner
  }

  var indexPathOrg:String = _
  def indexPath():String = {
    if(indexPathOrg == null){
      indexPathOrg = vertxContext.config().get.getString("indexPath", "indexes")
      if(!indexPathOrg.endsWith(File.separator)){
        indexPathOrg += File.separator
      }
    }
    indexPathOrg
  }

  var articlePathOrg:String = _
  def articlePath():String = {
    if(articlePathOrg == null){
      articlePathOrg = vertxContext.config().get.getString("articlePath", "articles")
      if(!articlePathOrg.endsWith(File.separator)){
        articlePathOrg += File.separator
      }
    }
    articlePathOrg
  }

  def timestampFile():String = indexPath() + "index.ts"

  def refreshTimerInterval(): Long = vertxContext.config().get.getInteger("refreshIndexPerSecond", 300) * 1000L

  val ID: String = "id"
  val TITLE: String = "title"
  val AUTHOR:String = "author"
  val CONTENTS: String = "contents"

  val MAX_SEARCH: Int = 30 //单次搜索默认最大返回文章个数
  val FRAGMENT_SIZE:Int = 150 //片段最大长度
  val MAX_HIGHLIGHTER:Int = 10 //最多的高亮次数

  val LINE_SEPARATOR:String = System.getProperty("line.separator", "\n")

  val analyzer: HanLPAnalyzer = new HanLPAnalyzer
}
