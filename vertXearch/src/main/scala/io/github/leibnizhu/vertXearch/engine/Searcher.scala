package io.github.leibnizhu.vertXearch.engine

import java.nio.file.Paths

import io.github.leibnizhu.vertXearch.utils.Constants._
import org.apache.lucene.document.Document
import org.apache.lucene.index.{DirectoryReader, MultiFields}
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory
class Searcher(indexDirectoryPath: String) {

  private val indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath))
  private var reader: DirectoryReader = DirectoryReader.open(indexDirectory)
  var indexSearcher = new IndexSearcher(reader)
  var queryParser = new MultiFieldQueryParser(Array(/*TITLE, AUTHOR,*/CONTENTS), ANALYZER)

  def search(searchQuery: String, length: Int = MAX_SEARCH): (Query, List[Document]) = {
    val query = queryParser.parse(searchQuery.toLowerCase)
    (query, topDocsToDocumentList(indexSearcher.search(query, length)))
  }

  def topDocsToDocumentList(topDocs: TopDocs): List[Document] = topDocs.scoreDocs.map(this.getDocument).toList

  def getDocument(scoreDoc: ScoreDoc): Document = indexSearcher.doc(scoreDoc.doc)

  def close(): Unit = {
    reader.close()
    indexDirectory.close()
  }

  /**
    * 刷新IndexSearcher
    * 如果文件夹内有修改,则openIfChanged()返回非null,此时更新IndexReader和IndexSearcher
    * 否则啥也不干
    */
  def refreshIndexSearcher(): Unit = {
    val oldReader = this.reader
    val newReader = DirectoryReader.openIfChanged(reader)
    if (newReader != null) {
      this.reader = newReader
      this.indexSearcher = new IndexSearcher(this.reader)
      oldReader.close()
    }
  }

  /**
    * 获取所有有效文档
    *
    * @return
    */
  def getAllDocuments: List[Document] = {
    //获取有哪些存活的文档
    val liveDocs = MultiFields.getLiveDocs(reader)
    if (liveDocs != null)
    //liveDocs非null时有删除过文件,遍历所有文档ID,liveDocs.get为true的话就是存活的,要过滤存活的文档对象
      (0 until reader.maxDoc()).filter(liveDocs.get).map(reader.document(_)).toList
    else
    //没有删除过文件的时候liveDocs为null,此时只能直接通过IndexSearcher去查询
      topDocsToDocumentList(indexSearcher.search(new MatchAllDocsQuery, Integer.MAX_VALUE))
  }
}
