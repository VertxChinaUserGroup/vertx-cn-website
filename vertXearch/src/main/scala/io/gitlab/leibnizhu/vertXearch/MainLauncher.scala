package io.gitlab.leibnizhu.vertXearch

import io.gitlab.leibnizhu.vertXearch.verticle.HttpSearchVerticle
import io.vertx.core.json.JsonObject
import io.vertx.scala.core.{DeploymentOptions, Vertx}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

object MainLauncher {
  private val log = LoggerFactory.getLogger(classOf[MainLauncher])

  def main(args: Array[String]): Unit = {
    //Force to use slf4j
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
    System.setProperty("vertx.disableFileCaching", "true")
    val vertx = Vertx.vertx
    if(args.length == 1){
      val configFile = args(0)
      vertx.fileSystem().readFileFuture(configFile).onComplete{
        case Success(result) =>
          log.info("读取配置文件{}成功,准备启动Verticle.", configFile)
          vertx.deployVerticle(s"scala:${classOf[HttpSearchVerticle].getName}",
            DeploymentOptions().setConfig(new JsonObject(result)))
        case Failure(cause) =>
          log.error("读取配置文件失败.", cause)
          System.exit(1)
      }
    } else {
      vertx.deployVerticle(s"scala:${classOf[HttpSearchVerticle].getName}")
    }
  }
}

class MainLauncher