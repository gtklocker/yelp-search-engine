package ir

import org.apache.lucene.index.Term
import org.apache.lucene.search.{IndexSearcher, Query, BooleanQuery,
BooleanClause, TopDocs, SortField, Sort}
import org.apache.lucene.document.{Document, LatLonPoint}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.analysis.standard.StandardAnalyzer

class BusinessSortField(val field: String, val fieldType: SortField.Type, reverse: Boolean) extends SortField(field, fieldType, reverse) {
  def this(field: String, fieldType: SortField.Type) = this(field, fieldType, false)
}
object SortByReviewCount extends BusinessSortField("business.reviewCount", SortField.Type.LONG)
object SortByStars extends BusinessSortField("business.stars", SortField.Type.FLOAT, true)

class ReviewSortField(val field: String, val fieldType: SortField.Type, reverse: Boolean) extends SortField(field, fieldType, reverse) {
  def this(field: String, fieldType: SortField.Type) = this(field, fieldType, false)
}
object SortByUseful extends ReviewSortField("review.useful", SortField.Type.LONG)
object SortByDate extends ReviewSortField("review.date", SortField.Type.LONG, true)

object Searcher {
  val MAX_HITS = 100
  val MAX_LOCATION_RADIUS = 20 * 1000 // 20km

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
    docsForQuery(BusinessIndex.searcher, query, sortedBy)
      .map(BusinessHit.fromDocument)
  }

  def representativeBusinesses(businesses: List[BusinessHit], sortBy: Option[BusinessSortField]): List[BusinessHit] = {
    val ret = businesses.groupBy(_.stars).mapValues(_.take(2)).values.toList.flatten
    sortBy match {
      case Some(SortByReviewCount) => ret.sortWith(_.reviewCount > _.reviewCount)
      case Some(SortByStars) => ret.sortWith(_.stars > _.stars)
      case _ => ret
    }
  }

  def representativeReviews(reviews: List[ReviewHit], sortBy: Option[ReviewSortField]): List[ReviewHit] = {
    val ret = reviews.groupBy(_.date.getYear).mapValues(_.take(2)).values.toList.flatten
    sortBy match {
      case Some(SortByUseful) => ret.sortWith(_.useful > _.useful)
      case Some(SortByDate) => ret.sortWith((a, b) => a.date.after(b.date))
      case _ => ret
    }
  }

  def businessHasReviewContaining(text: String): Query =
    queryFieldContains("business.reviewText", text)

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
    docsForQuery(ReviewIndex.searcher, query, sortedBy)
      .map(ReviewHit.fromDocument)
  }

  def reviewForBusinessWithName(businessName: String): Query =
    queryFieldContains("review.business.name", businessName)

  def reviewContains(text: String): Query =
    queryFieldContains("review.text", text)

  def queryFieldContains(field: String, text: String): Query = {
    val queryParser = new QueryParser(field, new StandardAnalyzer)
    queryParser.parse(text)
  }

  def docsForQuery(searcher: IndexSearcher, query: Query, sortedBy: Option[SortField]): List[Document] = {
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
