package ir

import org.scalatra._
import org.scalatra.json._
import org.json4s.{DefaultFormats, Formats}

class SearcherAPI extends ScalatraServlet with JacksonJsonSupport with CorsSupport {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats

  def nonempty(str: Option[String]): Option[String] = str match {
    case Some("") => None
    case _ => str
  }

  before() {
    contentType = formats("json")
    response.setHeader("Access-Control-Allow-Origin", "*")
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
    Searcher.findBusinesses(nonempty(params.get("text")), location, sortBy)
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
