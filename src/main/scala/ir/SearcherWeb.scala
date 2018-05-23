package ir

import org.scalatra._

class SearcherWeb extends ScalatraServlet {
  get("/search/businesses") {
    views.html.businessSearch()
  }

  get("/search/reviews") {
    views.html.reviewSearch()
  }

  get("/") {
    redirect("/search/businesses")
  }

  // HTML forms push empty inputs, treat those as None
  def nonempty(str: Option[String]): Option[String] = str match {
    case Some("") => None
    case _ => str
  }

  get("/results/businesses") {
    val representative = nonempty(params.get("representative"))
    val location = (nonempty(params.get("latitude")), nonempty(params.get("longitude"))) match {
      case (Some(lat), Some(long)) => Some((lat.toDouble, long.toDouble))
      case _                       => None
    }
    val sortBy = nonempty(params.get("sortBy")) match {
      case Some("reviewCount") => Some(SortByReviewCount)
      case Some("stars")       => Some(SortByStars)
      case _                   => None
    }
    val initialSortBy = if (representative.isDefined) None else sortBy
    var hits = Searcher.findBusinesses(nonempty(params.get("text")), location, initialSortBy)
    if (representative.isDefined) {
      hits = Searcher.representativeBusinesses(hits, sortBy)
    }
    views.html.businessResults(hits)
  }

  get("/results/reviews") {
    val representative = nonempty(params.get("representative"))
    val sortBy = nonempty(params.get("sortBy")) match {
      case Some("useful")   => Some(SortByUseful)
      case Some("date")     => Some(SortByDate)
      case _                => None
    }
    val initialSortBy = if (representative.isDefined) None else sortBy
    var hits = Searcher.findReviews(nonempty(params.get("businessName")), nonempty(params.get("text")), initialSortBy)
    if (representative.isDefined) {
      hits = Searcher.representativeReviews(hits, sortBy)
    }
    views.html.reviewResults(hits)
  }
}
