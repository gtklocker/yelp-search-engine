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

  for (business <- findBusinessesByReview(Some("bitch"), Some(51.509865, -0.118092))) {
    val review = business.getField("review").stringValue
    val businessName = business.getField("business_name").stringValue
    println(s"Business ${businessName}: ${review.substring(0, 140)}...")
  }

  def findBusinessesByReview(text: Option[String], location: Option[(Double, Double)]): Array[Document] = {
    val queryBuilder = new BooleanQuery.Builder()
    if (text.isDefined) {
      queryBuilder.add(reviewContainsText(text.get), BooleanClause.Occur.SHOULD)
    }
    if (location.isDefined) {
      queryBuilder.add(reviewForBusinessNearLocation(location.get), BooleanClause.Occur.SHOULD)
    }
    return docsForQuery(queryBuilder.build())
  }

  def reviewContainsText(text: String): Query =
    new TermQuery(new Term("review", text))

  def reviewForBusinessNearLocation(location: (Double, Double)): Query =
    LatLonPoint.newDistanceQuery("location", location._1, location._2, MAX_LOCATION_RADIUS)

  def docsForQuery(query: Query): Array[Document] = {
    val results = searcher.search(query, MAX_HITS)
    return results.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
  }
}
