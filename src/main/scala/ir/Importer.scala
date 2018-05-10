package ir

import DocumentMakers.{makeReviewDocument, makeBusinessDocument}

import org.apache.lucene.document.DateTools.Resolution

object Importer extends App {
  println("Starting import... to %s".format(Lucene.indexPath.toString()))

  val writer = Lucene.writer
  val db = new Database

  indexReviewDocuments()
  indexBusinessDocuments()

  println(s"${writer.numDocs} document(s) indexed!")

  db.close
  writer.close

  println("Import done.")
  
  def indexReviewDocuments(){  
    println("Importing review documents: ")
    val rs = db.queryResults("""select business.name, text, date, useful
                               |from review
                               |join business
                               |on review.business_id = business.id
                               """.stripMargin)
    
    while (rs.next) {
      writer.addDocument(makeReviewDocument(
        businessName = rs.getString("business.name"),
        text = rs.getString("text"),
        date = rs.getDate("date").getTime(),
        useful = rs.getLong("useful")
      ))
      print(".")
    }
    println("")
  }

  def indexBusinessDocuments() {
    println("Importing business documents: ")
    val rs = db.queryResults("""select business_id, name, business.stars, group_concat(review.text, " ")
                               |as text, count(*) as reviewCount, latitude, longitude
                               |from business join review
                               |on business.id = review.business_id
                               |group by business_id
                               |having count(*) >= 100
                               """.stripMargin)
    while (rs.next) {
      writer.addDocument(makeBusinessDocument(
        id = rs.getString("business_id"),
        name = rs.getString("name"),
        allReviews = rs.getString("text"),
        reviewCount = rs.getLong("reviewCount"),
        stars = rs.getFloat("stars"),
        latitude = rs.getFloat("latitude"),
        longitude = rs.getFloat("longitude")
      ))
      print(".")
    }
    println("")
  }
}
