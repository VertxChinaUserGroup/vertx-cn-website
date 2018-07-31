package io.github.leibnizhu.vertXearch.utils

import Constants.MAX_SEARCH
import io.vertx.scala.core.http.HttpServerRequest

import scala.util.Try

object HttpRequestUtil {

  val REQ_PARAM_KEYWORD = "keyword"
  val REQ_PARAM_LENGTH = "length"

  def parseRequestParam(request: HttpServerRequest): (String, Int) = {
    val keyword = request.getParam(REQ_PARAM_KEYWORD).getOrElse("")
    val lengthOption = request.getParam(REQ_PARAM_LENGTH)
    val length = Try(lengthOption.map(_.toInt).getOrElse(MAX_SEARCH)).getOrElse(MAX_SEARCH) //第一个getOrElse为无传入参数,第二个getOrElse为传入参数无法解析
    (keyword, Math.max(1, length))
  }
}
