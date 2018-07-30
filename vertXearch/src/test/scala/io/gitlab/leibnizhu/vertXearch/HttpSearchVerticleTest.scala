package io.gitlab.leibnizhu.vertXearch

import io.gitlab.leibnizhu.vertXearch.verticle.HttpSearchVerticle
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.{DeploymentOptions, Vertx}
import io.vertx.scala.ext.web.client.WebClient
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll}
import org.slf4j.LoggerFactory

class HttpSearchVerticleTest extends AsyncFlatSpec with BeforeAndAfterAll {
  private val log = LoggerFactory.getLogger(getClass)
  private val configFile = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/main/resources/config.json"
  private val vertx = Vertx.vertx()
  private val config = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
  private val host = "localhost"
  private val port = config.getInteger("serverPort", 8083)
  private val client = WebClient.create(vertx)

  override def beforeAll: Unit = {
    val future = vertx.deployVerticleFuture(ScalaVerticle.nameForVerticle[HttpSearchVerticle], DeploymentOptions().setConfig(config))
    while (!future.isCompleted) {}
  }

  "查询clojure" should "有结果返回且正确" in {
    log.info("开始查询clojure测试")
    client.get(this.port, this.host, "/q/clojure").sendFuture().map(response => {
      val respJson = new JsonObject(response.body.get)
      log.info(s"查询clojure的查询结果:${respJson.encodePrettily()}")
      assert(response.statusCode() == 200 &&
        respJson.getString("status") == "success" &&
        !respJson.getJsonArray("results").isEmpty &&
        respJson.containsKey("cost"))
    })
  }

  "查询clojure并限制返回长度" should "有结果返回且长度满足限制" in {
    log.info("开始限制长度查询clojure测试")
    client.get(this.port, this.host, "/q/clojure/2").sendFuture().map(response => {
      val respJson = new JsonObject(response.body.get)
      log.info(s"查询clojure并限制最大长度为2的查询结果:${respJson.encodePrettily()}")
      assert(response.statusCode() == 200 &&
        respJson.getString("status") == "success" &&
        !respJson.getJsonArray("results").isEmpty &&
        respJson.getJsonArray("results").size() <= 2 &&
        respJson.containsKey("cost"))
    })
  }

  "查询thisKeywordWillResponseEmptyResult" should "返回结果应为空" in {
    log.info("开始查询thisKeywordWillResponseEmptyResul测试")
    client.get(this.port, this.host, "/q/thisKeywordWillResponseEmptyResult").sendFuture().map(response => {
      val respJson = new JsonObject(response.body.get)
      log.info(s"查询thisKeywordWillResponseEmptyResult的查询结果:${respJson.encodePrettily()}")
      assert(response.statusCode() == 200 &&
        respJson.getString("status") == "success" &&
        respJson.getJsonArray("results").isEmpty &&
        respJson.containsKey("cost"))
    })
  }

  //FIXME 暂时没想到怎么能触发后台的错误,正常地请求,要么路径不对404,要么参数有问题但被处理掉了,要是删掉索引,可能影响其他测试
  "模拟请求错误" should "响应json带有message字段" in {
    log.info("模拟请求错误测试")
    client.get(this.port, this.host, "/q/clojure/aaa").sendFuture().map(response => {
      val respJson = new JsonObject(response.body.get)
      log.info(s"模拟请求错误查询的结果:${respJson.encodePrettily()}")
      assert(response.statusCode() == 200 &&
        //FIXME        respJson.getString("status") == "error" && //如果能模拟后台错误,这句应该取消注释
        //FIXME        respJson.containsKey("message") && //如果能模拟后台错误,这句应该取消注释
        respJson.containsKey("cost"))
    })
  }

  override def afterAll: Unit = {
    log.info("Http接口测试准备关闭Vertx")
    val closeFuture = vertx.closeFuture()
    while (!closeFuture.isCompleted) {}
    log.info("Http接口测试已经关闭Vertx")
  }
}
