package ir

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, BooleanQuery, BooleanClause, TopDocs}
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.document.LatLonPoint

object Searcher extends App {
  val reader = DirectoryReader.open(Lucene.directory)
  val searcher = new IndexSearcher(reader)
  val MAX_HITS = 20
  val MAX_LOCATION_RADIUS = 50

  searchBusinessesByReview(Some("bitch"), Some(51.509865, -0.118092))

  def searchBusinessesByReview(text: Option[String], location: Option[(Double, Double)]) {
    val queryBuilder = new BooleanQuery.Builder()
    if (text.isDefined) {
      queryBuilder.add(reviewContainsText(text.get), BooleanClause.Occur.SHOULD)
    }
    if (location.isDefined) {
      queryBuilder.add(reviewForBusinessNearLocation(location.get), BooleanClause.Occur.SHOULD)
    }
    val query = queryBuilder.build()

    val results = searcher.search(query, MAX_HITS)
    println(s"${results.totalHits} result(s).")

    val hits = results.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
    for (hit <- hits) {
      val review = hit.getField("review").stringValue
      val businessName = hit.getField("business_name").stringValue
      println(s"Business ${businessName}: ${review.substring(0, 140)}...")
    }
  }

  def reviewContainsText(text: String): Query =
    new TermQuery(new Term("review", text))
  def reviewForBusinessNearLocation(location: (Double, Double)): Query =
    LatLonPoint.newDistanceQuery("location", location._1, location._2, MAX_LOCATION_RADIUS)
}
