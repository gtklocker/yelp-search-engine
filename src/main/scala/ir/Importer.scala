package ir

import DocumentMakers.{makeReviewDocument, makeBusinessDocument}

import org.apache.lucene.document.DateTools.Resolution
import org.apache.lucene.document.{Document, TextField, StringField,
LatLonPoint, Field, StoredField, NumericDocValuesField, FloatDocValuesField,
DateTools}

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

    val businessIdToDoc = scala.collection.mutable.Map[String, Document]()
    val rs = db.queryResults("""select id, name, stars, latitude, longitude
                               |from business
                               """.stripMargin)
    while (rs.next) {
      val businessId = rs.getString("id")
      businessIdToDoc(businessId) = makeBusinessDocument(
        id = businessId,
        name = rs.getString("name"),
        stars = rs.getFloat("stars"),
        latitude = rs.getFloat("latitude"),
        longitude = rs.getFloat("longitude")
      )
      print(".")
    }

    val reviewRs = db.queryResults("""select business_id, text
                                      |from review
                                      """.stripMargin)
    while (reviewRs.next) {
      val businessId = reviewRs.getString("business_id")
      val text = reviewRs.getString("text")
      val doc = businessIdToDoc(businessId)
      doc.add(new TextField("business.allReviews", text, Field.Store.YES))
      print(",")
    }

    businessIdToDoc foreach {
      case (_, doc) => {
        val reviewCount = doc.getValues("business.allReviews").length
        doc.add(new NumericDocValuesField("business.reviewCount", reviewCount))
        doc.add(new StoredField("business.reviewCount", reviewCount))
        writer.addDocument(doc)
      }
    }

    println("")
  }
}
