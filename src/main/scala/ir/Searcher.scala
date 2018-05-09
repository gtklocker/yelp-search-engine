package ir

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, BooleanQuery, BooleanClause, TopDocs}
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.document.{Document, LatLonPoint}

object Searcher extends App {
  val reader = DirectoryReader.open(Lucene.directory)
  val searcher = new IndexSearcher(reader)
  val MAX_HITS = 20
  val MAX_LOCATION_RADIUS = 50

  val hits = findBusinesses(Some("good"), None)
  println(s"Got ${hits.length} business hits.")
  for (business <- hits) {
    println(s"Business ${business.name}: ${business.allReviews.substring(0, 140)}...")
  }

  def findBusinesses(text: Option[String], location: Option[(Double, Double)]): Array[BusinessHit] = {
    val queryBuilder = new BooleanQuery.Builder()
    if (text.isDefined) {
      queryBuilder.add(businessHasReviewContaining(text.get), BooleanClause.Occur.SHOULD)
    }
    if (location.isDefined) {
      queryBuilder.add(businessNearLocation(location.get), BooleanClause.Occur.SHOULD)
    }
    docsForQuery(queryBuilder.build())
      .map(BusinessHit.fromDocument)
  }

  def businessHasReviewContaining(text: String): Query =
    new TermQuery(new Term("business.allReviews", text))

  def businessNearLocation(location: (Double, Double)): Query =
    LatLonPoint.newDistanceQuery("business.location", location._1, location._2, MAX_LOCATION_RADIUS)

  def docsForQuery(query: Query): Array[Document] = {
    val results = searcher.search(query, MAX_HITS)
    results.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
  }
}
