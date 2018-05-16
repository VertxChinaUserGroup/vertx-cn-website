package io.gitlab.leibnizhu.vertXearch

import io.gitlab.leibnizhu.vertXearch.Constants._
import io.vertx.core.{AsyncResult, Future, Handler}
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.lang.scala.json.JsonObject
import io.vertx.scala.core.http.HttpServer
import io.vertx.scala.ext.web.handler.StaticHandler
import io.vertx.scala.ext.web.{Router, RoutingContext}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

class MainVerticle extends ScalaVerticle {
  private val log = LoggerFactory.getLogger(getClass)
  private var mainRouter: Router = _
  private var server: HttpServer = _
  private var searchEngine: Engine = _

  override def start(): Unit = {
    super.start()
    val future: Future[Unit] = Future.future()
    future.setHandler(_ => { //初始化工具类/组件
      mountRouters() //挂载所有子路由
      startServer(); //启动服务器
    })
    initComponents(future)
  }

  private def initComponents(afterSearchEngineStarted: Future[Unit]): Unit = {
    Constants.init(ctx)
    this.mainRouter = Router.router(vertx)
    this.server = vertx.createHttpServer
    this.searchEngine = new EngineImpl(indexPath(), articlePath(), afterSearchEngineStarted)
  }

  def mountRouters(): Unit = {
    mainRouter.get("/static/*").handler(StaticHandler.create.setWebRoot("static"))
    mainRouter.get("/q/:keyword").handler(searchByKeyWord)
    mainRouter.get("/q/:keyword/:length").handler(searchByKeyWord)
  }

  private def searchByKeyWord: Handler[RoutingContext] = rc => {
    val startTime = System.currentTimeMillis()
    val req = rc.request
    val response = rc.response
    val keyWord = req.getParam("keyword").getOrElse("")
    val length = req.getParam("length").map(_.toInt).getOrElse(MAX_SEARCH)
    searchEngine.search(keyWord, length, res => {
      response.putHeader("content-type", "application/json;charset=UTF-8")
        .end(if (res.succeeded()) {
          val results = res.result()
          val costTime = System.currentTimeMillis() - startTime
          log.debug(s"查询:${keyWord}成功, 查询到${results.size}条结果, 耗时${costTime}毫秒")
          new JsonObject().put("status", "success").put("results", results.asJava).put("cost", costTime).toString
        } else {
          val cause = res.cause()
          log.error("查询失败", cause)
          new JsonObject().put("status", "error").put("message", cause.getClass.getName+":"+cause.getMessage).toString
        })
    })
  }

  /**
    * 启动服务器
    */
  private def startServer(): Unit = {
    val port = config.getInteger("serverPort", 8083)
    server.requestHandler(mainRouter.accept(_)).listen(port, (res: AsyncResult[HttpServer]) => {
      if (res.succeeded) {
        log.info("监听{}端口的HTTP服务器启动成功", port)
        searchEngine.startRefreshTimer(refreshTimerInterval())
      } else {
        log.error("监听{}端口的HTTP服务器失败，原因：{}", Seq[AnyRef](port, res.cause.getLocalizedMessage): _*)
      }
    })
  }

  override def stop(): Unit = {
    server.close(res => log.info("HTTP服务器关闭" + (if (res.succeeded) "成功" else "失败")))
    searchEngine.stop(res => log.info("搜索引擎关闭" + (if (res.succeeded) "成功" else "失败")))
    super.stop()
  }

}
