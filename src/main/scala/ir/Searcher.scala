package ir

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, TopDocs}
import org.apache.lucene.store.RAMDirectory

object Searcher extends App {
  val reader = DirectoryReader.open(Lucene.directory)
  val searcher = new IndexSearcher(reader)

  val searchTerm = "sunny"
  println(s"Searching for ${searchTerm}...")
  val query = new TermQuery(new Term("tweet", searchTerm))
  val results = searcher.search(query, 10)

  println(s"${results.totalHits} result(s).")

  val topResult = searcher.doc(results.scoreDocs(0).doc)
  val tweet = topResult.getField("tweet").stringValue
  val author = topResult.getField("author").stringValue
  println(s"Top result: @${author}: ${tweet}.")
}
