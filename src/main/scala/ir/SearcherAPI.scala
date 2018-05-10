package ir

import org.scalatra._

class SearcherAPI extends ScalatraServlet {
  def nonempty(str: Option[String]): Option[String] = str match {
    case Some("") => None
    case _ => str
  }

  get("/businesses") {
    val location = (nonempty(params.get("latitude")), nonempty(params.get("longitude"))) match {
      case (Some(lat), Some(long)) => Some((lat.toDouble, long.toDouble))
      case _                       => None
    }
    val sortBy = nonempty(params.get("sortBy")) match {
      case Some("reviewCount") => Some(SortByReviewCount)
      case Some("stars")       => Some(SortByStars)
      case _                   => None
    }
    val hits = Searcher.findBusinesses(nonempty(params.get("text")), location, sortBy)
    views.html.businessResults(hits)
  }

  get("/reviews") {
    val sortBy = nonempty(params.get("sortBy")) match {
      case Some("useful")   => Some(SortByUseful)
      case Some("date")     => Some(SortByDate)
      case _                => None
    }
    Searcher.findReviews(nonempty(params.get("businessName")), nonempty(params.get("text")), sortBy)
  }
}
