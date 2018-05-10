package ir

import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}

class SearcherAPI extends ScalatraServlet with JacksonJsonSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  before() {
    contentType = formats("json")
  }

  get("/businesses") {
    val location = (params.get("latitude"), params.get("longitude")) match {
      case (Some(lat), Some(long)) => Some((lat.toDouble, long.toDouble))
      case _                       => None
    }
    val sortBy = params.get("sortBy") match {
      case Some("reviewCount") => Some(SortByReviewCount)
      case Some("stars")       => Some(SortByStars)
      case _                   => None
    }
    Searcher.findBusinesses(params.get("text"), location, sortBy)
  }

  get("/reviews") {
    val sortBy = params.get("sortBy") match {
      case Some("useful")   => Some(SortByUseful)
      case Some("date")     => Some(SortByDate)
      case _                => None
    }
    Searcher.findReviews(params.get("businessName"), params.get("text"), sortBy)
  }
}
