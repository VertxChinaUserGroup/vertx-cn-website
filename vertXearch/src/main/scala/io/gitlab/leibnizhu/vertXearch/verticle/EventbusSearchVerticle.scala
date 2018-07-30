package io.gitlab.leibnizhu.vertXearch.verticle

import io.gitlab.leibnizhu.vertXearch.engine.{Engine, EngineImpl}
import io.gitlab.leibnizhu.vertXearch.utils.Constants.{articlePath, indexPath}
import io.gitlab.leibnizhu.vertXearch.utils.EventbusRequestUtil.Method.{ADD_ARTICLE, SEARCH}
import io.gitlab.leibnizhu.vertXearch.utils.EventbusRequestUtil._
import io.gitlab.leibnizhu.vertXearch.utils.ResponseUtil.{failSearch, successSearch}
import io.gitlab.leibnizhu.vertXearch.utils.{Article, Constants, EventbusRequestUtil}
import io.vertx.core.json.JsonObject
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Future
import io.vertx.scala.core.eventbus.Message
import org.slf4j.LoggerFactory

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

class EventbusSearchVerticle extends ScalaVerticle{
  private val log = LoggerFactory.getLogger(getClass)
  private var searchEngine: Engine = _

  override def startFuture(): concurrent.Future[_] = {
    val promise = Promise[Unit]()
    Constants.init(ctx)
    val eventbusAddress = config.getString("eventbusAddress", "search") //EventBus监听地址
    vertx.eventBus.consumer[JsonObject](eventbusAddress).handler(handleEventbusMessage) //启动监听Eventbus
    this.searchEngine = new EngineImpl(indexPath, articlePath)
      .init(Future.future[Unit]().setHandler(ar =>
        if (ar.succeeded())
          promise.success(())
        else
          promise.failure(ar.cause())
      ))
    promise.future
  }

  override def stop(): Unit = {
    searchEngine.stop(Future.future().setHandler(res => log.info("搜索引擎关闭" + (if (res.succeeded) "成功" else "失败"))))
    super.stop()
  }

  private def handleEventbusMessage(msg: Message[JsonObject]): Unit = {
    val msgBody = msg.body
    log.debug(s"接收到EventBus请求消息(${msg.address}),消息内容：$msgBody")
    //尝试解析请求的方法名
    val methodStr = msgBody.getString(REQ_METHOD_KEY)
    Try(Method.withName(methodStr)) match {
      case Success(ADD_ARTICLE) => handleAddArticleRequest(msg)
      case Success(SEARCH) => handleSearchRequest(msg)
      case Success(_) => handleOtherRequest(msg)
      case Failure(cause) =>
        log.error(s"错误的请求方法名:$methodStr, 异常信息:${cause.getMessage}")
        msg.fail(404, s"错误的请求方法名,请求字段'$REQ_METHOD_KEY'=$methodStr")
    }
  }

  def handleOtherRequest(msg: Message[JsonObject]): Unit = {
    msg.fail(405, "Unsupported method")
  }

  //TODO 完成新增文章到索引的功能,暂时不处理
  def handleAddArticleRequest(msg: Message[JsonObject]): Unit = {
    msg.reply("Not Finish yet")
  }

  def handleSearchRequest(msg: Message[JsonObject]): Unit = {
    val msgBody = msg.body()
    val startTime = System.currentTimeMillis()
    val keyword = EventbusRequestUtil.keywordFromRequest(msgBody)
    val length = EventbusRequestUtil.lengthFromRequest(msgBody)
    searchEngine.search(keyword, length, //防止传入的长度值小于等于0
      Future.future[List[Article]]().setHandler(ar => {
        val costTime = System.currentTimeMillis() - startTime
        if (ar.succeeded()) {
          val results = ar.result()
          log.debug(s"查询关键词'$keyword'成功, 查询到${results.size}条结果, 耗时${costTime}毫秒")
          msg.reply(successSearch(results, costTime))
        } else {
          val cause = ar.cause()
          log.error(s"查询关键词'$keyword'失败, 耗时${costTime}毫秒", cause)
          msg.fail(500, failSearch(cause, costTime).toString)
        }
      }))
  }
}
