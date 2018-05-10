package ir

import org.apache.lucene.analysis.core.SimpleAnalyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig, Term}
import org.apache.lucene.search.{IndexSearcher, Query, TermQuery, BooleanQuery,
BooleanClause, TopDocs, SortField, Sort}
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.document.{Document, LatLonPoint}

class BusinessSortField(val field: String, val fieldType: SortField.Type) extends SortField(field, fieldType)
object SortByReviewCount extends BusinessSortField("business.reviewCount", SortField.Type.LONG)
object SortByStars extends BusinessSortField("business.stars", SortField.Type.FLOAT)

object Searcher extends App {
  val reader = DirectoryReader.open(Lucene.directory)
  val searcher = new IndexSearcher(reader)
  val MAX_HITS = 20
  val MAX_LOCATION_RADIUS = 50

  val hits = findBusinesses(Some("good"), None, sortedBy = Some(SortByStars))
  println(s"Got ${hits.length} business hits.")
  for (business <- hits) {
    println(s"Business ${business.name}: ${business.allReviews.substring(0, 140)}...")
  }

  def findBusinesses(text: Option[String], location: Option[(Double, Double)],
      sortedBy: Option[BusinessSortField]): Array[BusinessHit] = {
    val queryBuilder = new BooleanQuery.Builder()
    if (text.isDefined) {
      queryBuilder.add(businessHasReviewContaining(text.get), BooleanClause.Occur.SHOULD)
    }
    if (location.isDefined) {
      queryBuilder.add(businessNearLocation(location.get), BooleanClause.Occur.SHOULD)
    }

    val query = queryBuilder.build()
    docsForQuery(query, sortedBy)
      .map(BusinessHit.fromDocument)
  }

  def businessHasReviewContaining(text: String): Query =
    new TermQuery(new Term("business.allReviews", text))

  def businessNearLocation(location: (Double, Double)): Query =
    LatLonPoint.newDistanceQuery("business.location", location._1, location._2, MAX_LOCATION_RADIUS)

  def docsForQuery(query: Query, sortedBy: Option[SortField]): Array[Document] = {
    val results = sortedBy match {
      case Some(sortField) => searcher.search(query, MAX_HITS, new Sort(sortField))
      case None            => searcher.search(query, MAX_HITS)
    }
    results.scoreDocs.map(scoreDoc => searcher.doc(scoreDoc.doc))
  }
}
