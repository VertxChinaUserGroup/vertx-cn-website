package io.gitlab.leibnizhu.vertXearch

import io.vertx.lang.scala.json.JsonObject

import scala.collection.JavaConverters._

object ResponseUtil {
  def successSearch(results: List[Article], costTime: Long): String =
    new JsonObject().
      put("status", "success")
      .put("results", results.asJava)
      .put("cost", costTime)
      .toString

  def failSearch(cause: Throwable, costTime: Long): String =
    new JsonObject()
      .put("status", "error")
      .put("message", s"${cause.getClass.getName}:${cause.getMessage}")
      .put("cost", costTime)
      .toString
}
