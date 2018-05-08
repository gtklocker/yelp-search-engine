package ir

import java.sql.{Connection,DriverManager,Statement}
import org.apache.lucene.document.DateTools.Resolution
import org.apache.lucene.document.{Document, TextField, StringField, LatLonDocValuesField, Field, FloatDocValuesField,NumericDocValuesField,DateTools}
import org.apache.lucene.index.IndexWriter

object Importer extends App {
  println("Starting import... to %s".format(Lucene.indexPath.toString()))

  Class.forName("com.mysql.jdbc.Driver")
  val url = "jdbc:mysql://localhost:3306/yelp_db"
  val username = "root"
  val password = "root"
  var connection = DriverManager.getConnection(url, username, password)
  val writer = new IndexWriter(Lucene.directory, Lucene.writerConfig)
  indexReviewDocuments()
  indexBusinessDocuments()

  println(s"${writer.numDocs} document(s) indexed!")

  connection.close
  writer.close

  println("Import done.")
  
  def indexReviewDocuments(){  
    println("Importing review documents: ")
    val rs = queryResults("""select business.name, text, date, useful
                             |from review
                             |join business
                             |on review.business_id = business.id
                             """.stripMargin)
    
    while (rs.next) {
      writer.addDocument(makeReviewDocument(
        businessName = rs.getString("business.name"),
        reviewText = rs.getString("text"),
        date = rs.getDate("date").getTime(),
        useful = rs.getLong("useful")
      ))
      print(".")
    }
    println("")
  }

  def makeReviewDocument(businessName: String, reviewText: String, date: Long,
      useful: Long): Document = {
    val doc = new Document
    doc.add(new StringField("business_name", businessName, Field.Store.YES))
    doc.add(new TextField("review_text", reviewText, Field.Store.YES))
    doc.add(new NumericDocValuesField("date", date))
    doc.add(new NumericDocValuesField("useful", useful))
    doc
  }

  def indexBusinessDocuments() {
    println("Importing business documents: ")
    val rs = queryResults("""select business_id, name, business.stars, group_concat(review.text, " ")
                             |as text, latitude, longitude
                             |from business join review
                             |on business.id = review.business_id
                             |group by business_id
                             |having count(*)>= 100
                             """.stripMargin)
    while (rs.next) {
      writer.addDocument(makeBusinessDocument(
        id = rs.getString("business_id"),
        name = rs.getString("name"),
        allReviews = rs.getString("text"),
        stars = rs.getFloat("stars"),
        latitude = rs.getFloat("latitude"),
        longitude = rs.getFloat("longitude")
      ))
      print(".")
    }
    println("")
  }

  def makeBusinessDocument(id: String, name: String, stars: Float, allReviews:
      String, latitude: Double, longitude: Double): Document = {
    val doc = new Document
    doc.add(new StringField("business_id", id, Field.Store.YES))
    doc.add(new StringField("business_name", name, Field.Store.YES))
    doc.add(new FloatDocValuesField("stars", stars))
    doc.add(new TextField("review", allReviews, Field.Store.YES))
    doc.add(new LatLonDocValuesField("location", latitude, longitude))
    doc
  }

  def queryResults(query: String): java.sql.ResultSet = {
    val statement = createStreamingStatement(connection)
    statement.executeQuery(query)
  }

  def createStreamingStatement(connection: Connection): Statement = {
    val statement =
      connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY)
    statement.setFetchSize(Integer.MIN_VALUE)
    statement
  }
}
