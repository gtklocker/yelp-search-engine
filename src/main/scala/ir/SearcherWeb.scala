package ir

import org.scalatra._

class SearcherWeb extends ScalatraServlet {
  get("/") {
    views.html.businessSearch()
  }
}
