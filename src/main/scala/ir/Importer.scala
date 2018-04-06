package ir

import java.sql.{Connection,DriverManager}
import org.apache.lucene.document.{Document, TextField, StringField, Field}
import org.apache.lucene.index.IndexWriter

object Importer extends App {
  println("Starting import... to %s".format(Lucene.indexPath.toString()))

  val url = "jdbc:mysql://localhost:3306/yelp_db"
  val driver = "com.mysql.jdbc.Driver"
  val username = "root"
  val password = "root"
  var connection:Connection = _
  val writer = new IndexWriter(Lucene.directory, Lucene.writerConfig)
  try {
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val statement = connection.createStatement
    val query = """select business_id, name, text
                  |from business join review
                  |on business.id = review.business_id
                  |where review_count >= 100
                  |limit 10""".stripMargin
    val rs = statement.executeQuery(query)
    while (rs.next) {
      val businessId = rs.getString("business_id")
      val name = rs.getString("name")
      val review = rs.getString("text")
      println("businessId = %s, name = %s, text = %s".format(businessId, name, review))

      val doc = new Document
      doc.add(new StringField("business_id", businessId, Field.Store.YES))
      doc.add(new StringField("business_name", name, Field.Store.YES))
      doc.add(new TextField("review", review, Field.Store.YES))
      writer.addDocument(doc)
    }
  } catch {
    case e: Exception => e.printStackTrace
  }
  connection.close

  println(s"${writer.numDocs} document(s) indexed!")
  writer.close

  println("Import done.")
}
