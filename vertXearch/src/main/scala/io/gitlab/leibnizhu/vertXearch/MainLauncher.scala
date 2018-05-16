package io.gitlab.leibnizhu.vertXearch

import io.vertx.core.json.JsonObject
import io.vertx.scala.core.{DeploymentOptions, Vertx}
import org.slf4j.LoggerFactory

object MainLauncher {
  private val log = LoggerFactory.getLogger(classOf[MainLauncher])

  def main(args: Array[String]): Unit = { //Force to use slf4j
    //    DefaultChannelId.newInstance
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory")
    System.setProperty("vertx.disableFileCaching", "true")
    val vertx = Vertx.vertx
    if(args.length == 1){
      val configFile = args(0)
      vertx.fileSystem().readFile(configFile, res => {
        if (res.succeeded()) {
          log.info("读取配置文件{}成功,准备启动Verticle.", configFile)
          vertx.deployVerticle(s"scala:${classOf[MainVerticle].getName}",
            DeploymentOptions().setConfig(new JsonObject(res.result())))
        } else {
          log.error("读取配置文件失败.", res.cause())
          System.exit(1)
        }
      })
    } else {
      vertx.deployVerticle(s"scala:${classOf[MainVerticle].getName}")
    }
  }
}

class MainLauncher