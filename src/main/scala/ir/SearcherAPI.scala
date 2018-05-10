import org.scalatra._

class SearcherAPI extends ScalatraServlet {
  get("/") {
    <h1>Hello, world!</h1>
  }

  get("/hello") {
    <h1>Hello, world again!</h1>
  }
}
