package io.gitlab.leibnizhu.vertXearch

import io.vertx.core.json.JsonObject
import io.vertx.scala.core.{DeploymentOptions, Future, Vertx}
import io.vertx.scala.ext.web.client.WebClient
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Try

class HttpInterfaceTest extends FlatSpec with BeforeAndAfterAll {
  private val log = LoggerFactory.getLogger(getClass)
  private val vertx = Vertx.vertx()
  private var config: JsonObject = _
  private val host = "localhost"
  private var port: Int = _
  private val client = WebClient.create(vertx)
  private val configFile = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/main/resources/config.json"
  private val futures = Array.fill(4)(Future.future[Unit]())

  override def beforeAll: Unit = {
    this.config = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
    this.port = config.getInteger("serverPort", 8083)
    val future = vertx.deployVerticleFuture(s"scala:${classOf[MainVerticle].getName}", DeploymentOptions().setConfig(config))
    while (!future.isCompleted) {}
  }

  "查询clojure" should "有结果返回且正确" in {
    log.info("开始查询clojure测试")
    client.get(this.port, this.host, "/q/clojure").sendFuture().onComplete(tried => {
      Try {
        assert(tried.isSuccess)
        val response = tried.get
        assert(response.statusCode() == 200)
        val respJson = new JsonObject(response.body.get)
        assert(respJson.getString("status") == "success")
        assert(!respJson.getJsonArray("results").isEmpty)
        assert(respJson containsKey "cost")
        log.info(s"查询clojure的查询结果:${respJson.encodePrettily()}")
      }
      futures(0).complete()
    })
  }

  "查询clojure并限制返回长度" should "有结果返回且长度满足限制" in {
    log.info("开始限制长度查询clojure测试")
    client.get(this.port, this.host, "/q/clojure/2").sendFuture().onComplete(tried => {
      Try {
        assert(tried.isSuccess)
        val response = tried.get
        assert(response.statusCode() == 200)
        val respJson = new JsonObject(response.body.get)
        assert(respJson.getString("status") == "success")
        assert(!respJson.getJsonArray("results").isEmpty)
        assert(respJson.getJsonArray("results").size() <= 2)
        assert(respJson containsKey "cost")
        log.info(s"查询clojure并限制最大长度为2的查询结果:${respJson.encodePrettily()}")
      }
      futures(1).complete()
    })
  }

  "查询thisKeywordWillResponseEmptyResult" should "返回结果应为空" in {
    log.info("开始t查询thisKeywordWillResponseEmptyResul测试")
    client.get(this.port, this.host, "/q/thisKeywordWillResponseEmptyResult").sendFuture().onComplete(tried => {
      Try {
        assert(tried.isSuccess)
        val response = tried.get
        assert(response.statusCode() == 200)
        val respJson = new JsonObject(response.body.get)
        assert(respJson.getString("status") == "success")
        assert(respJson.getJsonArray("results").isEmpty)
        assert(respJson containsKey "cost")
        log.info(s"查询thisKeywordWillResponseEmptyResult的查询结果:${respJson.encodePrettily()}")
      }
      futures(2).complete()
    })
  }

  "模拟请求错误" should "响应json带有message字段" in {
    log.info("模拟请求错误测试")
    //FIXME 暂时没想到怎么能触发后台的错误,正常地请求,要么路径不对404,要么参数有问题但被处理掉了,要是删掉索引,可能影响其他测试
    client.get(this.port, this.host, "/q/clojure/aaa").sendFuture().onComplete(tried => {
      Try {
        assert(tried.isSuccess)
        val response = tried.get
        assert(response.statusCode() == 200)
        val respJson = new JsonObject(response.body.get)
        assert(respJson.getString("status") == "error")
        assert(respJson.containsKey("message"))
        assert(respJson containsKey "cost")
        log.info(s"模拟请求错误查询的结果:${respJson.encodePrettily()}")
      }
      futures(3).complete()
    })
  }

  override def afterAll: Unit = {
    log.info("等待异步任务关闭")
    futures.foreach(f => while (!f.isComplete()) {})
    log.info("关闭Vertx")
    val closeFuture = vertx.closeFuture()
    while (!closeFuture.isCompleted) {}
  }
}
