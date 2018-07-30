package io.gitlab.leibnizhu.vertXearch

import io.gitlab.leibnizhu.vertXearch.utils.EventbusRequestUtil._
import io.gitlab.leibnizhu.vertXearch.verticle.EventbusSearchVerticle
import io.vertx.core.eventbus.ReplyException
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.{DeploymentOptions, Vertx}
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll}
import org.slf4j.LoggerFactory

class EventbusSearchVerticleTest extends AsyncFlatSpec with BeforeAndAfterAll {
  private val log = LoggerFactory.getLogger(getClass)
  private val vertx = Vertx.vertx()
  private val configFile = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/main/resources/config.json"
  private val config = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
  private val searchEventbusAddress = config.getString("eventbusAddress", "search") //EventBus监听地址


  override def beforeAll: Unit = {
    val future = vertx.deployVerticleFuture(ScalaVerticle.nameForVerticle[EventbusSearchVerticle], DeploymentOptions().setConfig(config))
    while (!future.isCompleted) {}
  }

  "向eventbus发送查询请求,不限长度" should "返回相应查询结果" in {
    vertx.eventBus().sendFuture[JsonObject](searchEventbusAddress, searchRequest("clojure"))
      .map(msg => {
        val respJson = msg.body()
        log.info(s"查询clojure不限制长度:接收到EventBus响应消息内容：${respJson.encodePrettily()}")
        assert(respJson.getString("status") == "success" &&
          !respJson.getJsonArray("results").isEmpty &&
          respJson.containsKey("cost"))
      })
  }

  "向eventbus发送查询请求,限制长度为2" should "返回相应查询结果,长度最大为2" in {
    vertx.eventBus().sendFuture[JsonObject](searchEventbusAddress, searchRequest("clojure", 2))
      .map(msg => {
        val respJson = msg.body()
        log.info(s"查询clojure限制长度为2:接收到EventBus响应消息内容：${respJson.encodePrettily()}")
        assert(respJson.getString("status") == "success" &&
          !respJson.getJsonArray("results").isEmpty &&
          respJson.getJsonArray("results").size() <= 2 &&
          respJson.containsKey("cost"))
      })
  }

  "向eventbus发送查询请求,查询不可能存在的词" should "返回结果应为空" in {
    vertx.eventBus().sendFuture[JsonObject](searchEventbusAddress, searchRequest("thisKeywordWillResponseEmptyResult"))
      .map(msg => {
        val respJson = msg.body()
        log.info(s"查询thisKeywordWillResponseEmptyResult:接收到EventBus响应消息内容：${respJson.encodePrettily()}")
        assert(respJson.getString("status") == "success" &&
          respJson.getJsonArray("results").isEmpty &&
          respJson.containsKey("cost"))
      })
  }

  "向eventbus发送错误请求方法" should "返回404错误" in {
    recoverToExceptionIf[ReplyException] {
      vertx.eventBus().sendFuture[JsonObject](searchEventbusAddress, searchRequest("test").put(REQ_METHOD_KEY, "aaa"))
    } map (exp => {
      log.info(s"发送错误的请求方法:抛出异常,出错信息:$exp")
      assert(exp.failureCode() == 404)
    })
  }

  override def afterAll: Unit = {
    log.info("Eventbus测试准备关闭Vertx")
    val closeFuture = vertx.closeFuture()
    while (!closeFuture.isCompleted) {}
    log.info("Eventbus测试已经关闭Vertx")
  }
}
