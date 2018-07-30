package io.gitlab.leibnizhu.vertXearch

import io.gitlab.leibnizhu.vertXearch.engine.{Indexer, Searcher}
import io.gitlab.leibnizhu.vertXearch.utils.Constants.{CONTENTS, ID, init}
import io.vertx.scala.core.{Future, Vertx}
import org.apache.lucene.document.Document
import org.scalatest.FlatSpec
import org.slf4j.LoggerFactory

class LuceneTest extends FlatSpec {
  private val log = LoggerFactory.getLogger(getClass)
  val indexDir: String = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/test/data/Index"
  val dataDir: String = "/Users/leibnizhu/workspace/vertx-cn-website/vertXearch/src/test/data/Articles"
  var indexer: Indexer = _
  var searcher: Searcher = _

  private val keyWord = "clojure"
  s"在已经生成索引的情况下,查${keyWord}" should s"返回结果若非空则结果均包含$keyWord" in {
    init(Vertx.vertx().getOrCreateContext())
    val future = createIndex(Future.future[Int]())
    while (!future.isComplete) {}
    val documents = search(keyWord)
    if (documents.nonEmpty)
      assert(documents.forall(_.get(CONTENTS).contains(keyWord)))
  }

  private def search(searchQuery: String): List[Document] = {
    searcher = new Searcher(indexDir)
    val startTime = System.currentTimeMillis
    val (_, hitDocs) = searcher.search(searchQuery)
    val endTime = System.currentTimeMillis
    log.info(s"找到${hitDocs.size}篇文章, 耗时${endTime - startTime} ms.")
    log.info(s"查找到的文章ID=${hitDocs.map(_.get(ID))}")
    searcher.close()
    hitDocs
  }

  private def createIndex(handler: Future[Int]): Future[Int] = {
    indexer = new Indexer(indexDir)
    indexer.cleanAllIndex()
    var numIndexed = 0
    val startTime = System.currentTimeMillis
    val future = Future.future[Int]().setHandler(ar => {
      if (ar.succeeded()) {
        numIndexed = ar.result()
        val endTime = System.currentTimeMillis
        log.info(s"给${numIndexed}篇文章建立了索引, 耗时:${endTime - startTime} ms.")
        indexer.close()
        handler.complete(numIndexed)
      }
    })
    indexer.createIndex(dataDir, future)
    handler
  }
}
