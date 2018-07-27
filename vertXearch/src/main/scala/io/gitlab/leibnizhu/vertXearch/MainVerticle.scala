package io.gitlab.leibnizhu.vertXearch

import io.gitlab.leibnizhu.vertXearch.Constants._
import io.gitlab.leibnizhu.vertXearch.ResponseUtil._
import io.vertx.core.Handler
import io.vertx.lang.scala.ScalaVerticle
import io.vertx.scala.core.Future
import io.vertx.scala.core.http.HttpServer
import io.vertx.scala.ext.web.handler.StaticHandler
import io.vertx.scala.ext.web.{Router, RoutingContext}
import org.slf4j.LoggerFactory

import scala.concurrent.Promise
import scala.util.{Failure, Success, Try}

class MainVerticle extends ScalaVerticle {
  private val log = LoggerFactory.getLogger(getClass)
  private var mainRouter: Router = _
  private var server: HttpServer = _
  private var searchEngine: Engine = _

  override def startFuture(): concurrent.Future[_] = {
    val promise = Promise[Unit]()
    //初始化工具类/组件
    val startedFuture = Future.future[Unit]().setHandler(ar => {
      mountRouters() //挂载所有子路由
      startServer(); //启动服务器
      if (ar.succeeded()) promise.success(()) else promise.failure(ar.cause())
    })
    initComponents(startedFuture)
    promise.future
  }

  private def initComponents(afterSearchEngineStarted: Future[Unit]): Future[Unit] = {
    Constants.init(ctx)
    this.mainRouter = Router.router(vertx)
    this.server = vertx.createHttpServer
    this.searchEngine = new EngineImpl(indexPath, articlePath).init(afterSearchEngineStarted)
    afterSearchEngineStarted
  }

  def mountRouters(): Unit = {
    mainRouter.get("/static/*").handler(StaticHandler.create.setWebRoot("static"))
    mainRouter.get("/q/:keyword").handler(searchByKeyWord)
    mainRouter.get("/q/:keyword/:length").handler(searchByKeyWord)
  }

  private def searchByKeyWord: Handler[RoutingContext] = rc => {
    val startTime = System.currentTimeMillis()
    val (request, response) = (rc.request, rc.response)
    val keyWord = request.getParam("keyword").getOrElse("")
    val lengthOption = request.getParam("length")
    val length = Math.max(1,  Try(lengthOption.map(_.toInt).getOrElse(MAX_SEARCH)).getOrElse(MAX_SEARCH)) //防止传入的长度值小于等于0
    searchEngine.search(keyWord, length, Future.future[List[Article]]().setHandler(ar => {
      val costTime = System.currentTimeMillis() - startTime
      response.putHeader("content-type", "application/json;charset=UTF-8").end(
        if (ar.succeeded()) {
          val results = ar.result()
          log.debug(s"查询关键词'$keyWord'成功, 查询到${results.size}条结果, 耗时${costTime}毫秒")
          successSearch(results, costTime)
        } else {
          val cause = ar.cause()
          log.error(s"查询关键词'$keyWord'失败, 耗时${costTime}毫秒", cause)
          failSearch(cause, costTime)
        })
    }))
  }

  /**
    * 启动服务器
    */
  private def startServer(): Unit = {
    val port = config.getInteger("serverPort", 8083)
    server.requestHandler(mainRouter.accept(_)).listenFuture(port).onComplete {
      case Success(_) =>
        log.info("监听{}端口的HTTP服务器启动成功", port)
        searchEngine.startRefreshTimer(refreshTimerInterval)
      case Failure(cause) =>
        log.error("监听{}端口的HTTP服务器失败，原因：{}", Seq[AnyRef](port, cause.getLocalizedMessage): _*)
      }
  }

  override def stop(): Unit = {
    server.close(res => log.info("HTTP服务器关闭" + (if (res.succeeded) "成功" else "失败")))
    searchEngine.stop(Future.future().setHandler(res => log.info("搜索引擎关闭" + (if (res.succeeded) "成功" else "失败"))))
    super.stop()
  }

}
