package ir

import org.scalatra._

class SearcherWeb extends ScalatraServlet {
  get("/search/businesses") {
    views.html.businessSearch()
  }

  get("/search/reviews") {
    //views.html.restaurantSearch()
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

  get("/results/reviews") {
    val sortBy = nonempty(params.get("sortBy")) match {
      case Some("useful")   => Some(SortByUseful)
      case Some("date")     => Some(SortByDate)
      case _                => None
    }
    val hits = Searcher.findReviews(nonempty(params.get("businessName")), nonempty(params.get("text")), sortBy)
    views.html.reviewResults(hits)
  }
}
