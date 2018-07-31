package io.github.leibnizhu.vertXearch

import java.io.File

import io.github.leibnizhu.vertXearch.utils.{Article, Constants}
import io.github.leibnizhu.vertXearch.utils.{Article, Constants}
import io.vertx.core.json.JsonObject
import io.vertx.scala.core
import io.vertx.scala.core.Vertx
import org.scalatest.{AsyncFlatSpec, BeforeAndAfterAll}
import org.slf4j.LoggerFactory

import scala.concurrent.{Future, Promise}
import scala.util.Try

class ArticleTest extends AsyncFlatSpec with BeforeAndAfterAll {
  private val source: List[Article] = List(
    Article("10", "Vert.x上的Clojure语言支持（项目已开源）\n圆胖肿\nPrevious in 白木城全栈：在Vert.x上实现Clojure语言支持的探索\n\n前一篇文章探索了如何在Vert.x上实现Clojure语言包的支持，基本上做了一次可行性分析，最后发现，好像，似乎是可以的，于是我们说干就干，经过大概一周左右的各种攻坚，算是有了一个还算不错的结果^_^\n\n先说结论，子项目名称是vertx-lang-clojure，基本上跟Vert.x其他的语言包命名保持一致，已经开源在Github上，地址是：whitewoodcity/vertx-lang-clojure all issues, comments, prs, forks are welcomed.\n\n以下是整个过程的唠叨，Vert.x的语言支持在v2时代是通过人工，每一个语言一个接口一个接口实现过去，这样做的结果就是慢，不仅慢，而且效率低，维护成本高，会出现每一个语言特定的错误，每一个语言都要专门test一遍，对开发不利，所以在v3时代，项目组转入了通过使用mvel自动生成代码的做法，先通过Java将核心代码实现，然后暴露出相应的API，再通过annotation对每一个api做标记，然后通过mvel的模版功能，将这些api转换成相应语言的API，这就是vertx-codegen子项目。\n\n通过这种方式，第一批生成的是脚本，也就是面向过程（pp）也就是不需要强制封装的三个语言的api，分别是Groovy，Ruby和Javascript，因为不需要封装，做起来最快，唯一需要处理的就是把caseCamel驼峰命名法改成case_snake的命名方式，脚本多数都采用_分割的case_snake命名法；其次就是把Ceylon，Scala还有Kotlin给转换过去，这三个语言跟Java的冲突都没有那么明显，因为都是面向对象（oop）语言，在封装层面上，都要求封装成对象，至少都允许封装成对象，kt&sc还允许最高层次（top/1st level）是函数，但是这两个并不冲突，不需要对原有系统做多少改变即可实现，可专注于封装之后的API生成工作，而且这三个语言做的层次都有些不太一样，Kotlin的封装最浅，几乎用的都是Java自己的API，其次是Ceylon，通过codegen自动生成了一批Ceylon的API，Scala封装得最为深入，不仅通过codegen自动生成了相对应的API，还自己实现了一层符合Scala使用习惯的API。\n\n那我们知道，三种比较主流的编程范式（pp，oop和fp），Vert.x还缺少函数式编程（fp）的支持，于是我们去Haskell/http://eta-lang.org项目下面建议，希望作者将http://eta-lang.org也提供Vert.x的支持，经过群众们的踊跃投票，在纽约的作者将Vert.x的支持纳入了eta v0.2的计划之中，所以在不远的将来，我们有可能看到Haskell出现在Vert.x上；那函数式的两大流派，lisp&haskell，肿么能少了lisp呢？更何况在Vert.x V2的时候曾经出现了Clojure的语言支持，于是我们找到了原作者Toby，咨询了相关的问题，了解了情况之后，于是我们就琢磨，如何将Clojure搬到Vert.x上去，也算是为http://eta-lang.org探探路，算是一个练手吧。\n\n具体的可行性分析见前一篇文章，那这里说一下实现过程，首先是通过上手mvel模版以及codegen来实现代码自动生成，毕竟是开源的项目，所以官方文档很不完整，也不友好，好在经过我们不屑努力，总算搞明白了逻辑，原来是需要maven，用maven的compile命令便可自动生成代码，官方提供了一个starter项目，介绍了如何自动生成md文件，经过一番努力之后，将其成功执行，执行成功之后，就开始通过mvel的模版以及codegen提供的API，来自动生成Clojure代码，要特别感谢Clojure-Kit这个好用的插件，使得我们可以很方便滴在生成代码之后，就能看出生成的代码是否正确。"),
  )

  private val log = LoggerFactory.getLogger(getClass)
  private val vertx = Vertx.vertx()
  private val context = vertx.getOrCreateContext()
  private val configFile = "src/main/resources/config.json"
  private val config: JsonObject = new JsonObject(vertx.fileSystem().readFileBlocking(configFile))
  private val dataPath = config.getString("articlePath")

  override def beforeAll: Unit = {
    val contextConfig = context.config().get
    config.forEach(e => contextConfig.put(e.getKey, e.getValue))
    Constants.init(context)
  }

  "将Article对象写入到文件" should "不报错" in {
    Future.sequence(source.map(article => {
      val promise = Promise[Boolean]()
      article.writeToFile(tried => {
        log.info(s"写入文章(ID=${article.id})成功")
        promise.complete(Try(tried.isSuccess))
      })
      promise.future
    })).map(list => assert(list.forall(_ == true)))
  }

  "从文件读取解析成Article" should "跟写入的一样" in {
    Future.sequence(source.map(originArticle => {
      val file = new File(dataPath, s"${originArticle.id}.txt")
      val promise = Promise[Boolean]()
      Article.fromFile(file, core.Future.future[Article]().setHandler(ar => {
        log.info("读取文件" + file.getName + "成功")
        val article = ar.result()
        promise.complete(Try(ar.succeeded() && article == originArticle.toLowerCase))
      }))
      promise.future
    })).map(list => assert(list.forall(_ == true)))
  }

  override def afterAll: Unit = {
    log.info("Article测试准备关闭Vertx")
    val closeFuture = vertx.closeFuture()
    while (!closeFuture.isCompleted) {}
    log.info("Article测试已经关闭Vertx")
  }
}
