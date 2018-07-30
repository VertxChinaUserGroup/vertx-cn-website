package io.gitlab.leibnizhu.vertXearch.utils

import io.vertx.core.json.JsonObject

import scala.util.Try

object EventbusRequestUtil {
  val REQ_METHOD_KEY = "method"
  val REQ_CONTENT_KEY = "content"

  val REQ_CONTENT_KEYWORD_KEY = "keyword"
  val REQ_CONTENT_LENGTH_KEY = "length"

  object Method extends Enumeration {
    type Method = Value
    val ADD_ARTICLE: Method.Value = Value("add")
    val SEARCH: Method.Value = Value("search")
  }

  /**
    * 搜索请求格式:
    * {
    *   REQ_METHOD_KEY : "search",
    *   REQ_CONTENT_KEY: {
    *     REQ_CONTENT_KEYWORD_KEY: "搜索关键词",
    *     REQ_CONTENT_LENGTH_KEY: "搜索结果最大条数,选填"
    *   }
    * }
    */
  def searchRequest(keyword: String): JsonObject = new JsonObject()
    .put(REQ_METHOD_KEY, Method.SEARCH.toString)
    .put(REQ_CONTENT_KEY, new JsonObject()
      .put(REQ_CONTENT_KEYWORD_KEY, keyword))

  def searchRequest(keyword: String, length: Int): JsonObject = {
    val response = searchRequest(keyword)
    response.getJsonObject(REQ_CONTENT_KEY).put(REQ_CONTENT_LENGTH_KEY, length)
    response
  }

  //TODO 格式待定
  def addArticleRequest(article: Article): JsonObject = new JsonObject()
    .put(REQ_METHOD_KEY, Method.ADD_ARTICLE.toString)

  def keywordFromRequest(request: JsonObject): String =
    Option(request.getJsonObject(REQ_CONTENT_KEY).getString(REQ_CONTENT_KEYWORD_KEY)).getOrElse("")

  def lengthFromRequest(request: JsonObject): Int = Math.max(1, //输入值和1较大者,防止输入参数为负数或0
    Try(
      Option(request.getJsonObject(REQ_CONTENT_KEY).getInteger(REQ_CONTENT_LENGTH_KEY))
        .map(_.toInt).getOrElse(Constants.MAX_SEARCH) //给Option准备的,防止没有传入长度参数
    ).getOrElse(Constants.MAX_SEARCH)) //给Try准备的,防止传入参数无法解析成整数
}
