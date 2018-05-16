package io.gitlab.leibnizhu.vertXearch

import io.vertx.core.{Future, Handler}
import io.vertx.scala.core.Vertx

object LuceneTester {

  val indexDir: String = "/Users/leibnizhu/Desktop/Index"
  val dataDir: String = "/Users/leibnizhu/Desktop/Data"
  var indexer: Indexer = _
  var searcher: Searcher = _

  def main(args: Array[String]) {
    Constants.init(Vertx.vertx().getOrCreateContext())
    LuceneTester.createIndex(num => {
      LuceneTester.search("编译")
    })
  }

  private def createIndex(handler: Handler[Int]): Unit = {
    indexer = new Indexer(indexDir)
    indexer.cleanAllIndex()
    var numIndexed = 0
    val startTime = System.currentTimeMillis
    val future: Future[Int] = Future.future()
    future.setHandler(ar => {
      if (ar.succeeded()) {
        numIndexed = ar.result()
        val endTime = System.currentTimeMillis
        println(numIndexed + " File indexed, time taken: " + (endTime - startTime) + " ms")
        indexer.close()
        handler.handle(numIndexed)
      }
    })
    indexer.createIndex(dataDir, future)
  }

  private def search(searchQuery: String): Unit = {
    searcher = new Searcher(indexDir)
    val startTime = System.currentTimeMillis
    val (query, hitDocs) = searcher.search(searchQuery)
    val endTime = System.currentTimeMillis
    println(hitDocs.size + " documents found. Time :" + (endTime - startTime))
    for (doc <- hitDocs) {
      println("File: " + doc.get(Constants.ID))
    }
    searcher.close()
  }
}
