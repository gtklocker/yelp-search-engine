package ir

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, TopDocs}
import org.apache.lucene.store.RAMDirectory

object Searcher extends App {
  val reader = DirectoryReader.open(Lucene.directory)
  val searcher = new IndexSearcher(reader)

  val searchTerm = "job"
  println(s"Searching for ${searchTerm}...")
  val query = new TermQuery(new Term("review", searchTerm))
  val results = searcher.search(query, 10)

  println(s"${results.totalHits} result(s).")

  val topResult = searcher.doc(results.scoreDocs(0).doc)
  val review = topResult.getField("review").stringValue
  val businessName = topResult.getField("business_name").stringValue
  println(s"Top result: @${businessName}: ${review}.")
}
