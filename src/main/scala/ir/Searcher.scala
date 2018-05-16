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

class ReviewSortField(val field: String, val fieldType: SortField.Type, reverse: Boolean) extends SortField(field, fieldType, reverse) {
  def this(field: String, fieldType: SortField.Type) = this(field, fieldType, false)
}
object SortByUseful extends ReviewSortField("review.useful", SortField.Type.LONG)
object SortByDate extends ReviewSortField("review.date", SortField.Type.LONG, true)

object Searcher {
  val reader = DirectoryReader.open(Lucene.directory)
  val searcher = new IndexSearcher(reader)
  val MAX_HITS = 500
  val MAX_LOCATION_RADIUS = 500

  def findBusinesses(text: Option[String], location: Option[(Double, Double)],
      sortedBy: Option[BusinessSortField]): List[BusinessHit] = {
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

  def representativeBusinesses(businesses: List[BusinessHit]): List[BusinessHit] = {
    businesses.groupBy(_.stars).mapValues(_.take(2)).values.toList.flatten
  }

  def representativeReviews(reviews: List[ReviewHit]): List[ReviewHit] = {
    reviews.groupBy(_.date.getYear).mapValues(_.take(2)).values.toList.flatten
  }

  def businessHasReviewContaining(text: String): Query =
    new TermQuery(new Term("business.reviewText", text))

  def businessNearLocation(location: (Double, Double)): Query =
    LatLonPoint.newDistanceQuery("business.location", location._1, location._2, MAX_LOCATION_RADIUS)

  def findReviews(businessName: Option[String], text: Option[String], sortedBy:
    Option[ReviewSortField]): List[ReviewHit] = {
    val queryBuilder = new BooleanQuery.Builder()
    if (businessName.isDefined) {
      queryBuilder.add(reviewForBusinessWithName(businessName.get), BooleanClause.Occur.SHOULD)
    }
    if (text.isDefined) {
      queryBuilder.add(reviewContains(text.get), BooleanClause.Occur.SHOULD)
    }

    val query = queryBuilder.build()
    docsForQuery(query, sortedBy)
      .map(ReviewHit.fromDocument)
  }

  def reviewForBusinessWithName(businessName: String): Query =
    new TermQuery(new Term("review.business.name", businessName))

  def reviewContains(text: String): Query =
    new TermQuery(new Term("review.text", text))

  def docsForQuery(query: Query, sortedBy: Option[SortField]): List[Document] = {
    val results = sortedBy match {
      case Some(sortField) => searcher.search(query, MAX_HITS, new Sort(sortField))
      case None            => searcher.search(query, MAX_HITS)
    }
    results.scoreDocs
      .map(scoreDoc => searcher.doc(scoreDoc.doc))
      .toList
  }
}

object SearcherDemo extends App {
  //val businessHits = Searcher.findBusinesses(Some("bad"), None, sortedBy = Some(SortByStars))
  val businessHits = Searcher.findBusinesses(Some("bad"), None, sortedBy = None)
  println(s"Got ${businessHits.length} business hits.")
  for (business <- businessHits) {
    println(s"Business ${business.name} (${business.stars}): ${business.reviewText.substring(0, 140)}...")
  }

  /*
  val reviewHits = Searcher.findReviews(businessName = Some("pizza"), text = Some("good"), sortedBy = Some(SortByDate))
  println(s"Got ${reviewHits.length} review hits.")
  for (review <- reviewHits) {
    println(s"Review ${review.businessName}: ${review.text.substring(0, 140)}...")
  }
  */
}
