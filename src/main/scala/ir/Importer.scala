package ir

import DocumentMakers.{makeReviewDocument, makeBusinessDocument}

import org.apache.lucene.document.DateTools.Resolution
import org.apache.lucene.document.{Document, TextField, StringField,
LatLonPoint, Field, StoredField, NumericDocValuesField, FloatDocValuesField,
DateTools}
import me.tongfei.progressbar.ProgressBar
import java.sql.ResultSet

object Importer extends App {
  println("Starting import...")

  val reviewWriter = ReviewIndex.writer
  val businessWriter = BusinessIndex.writer

  val db = new Database
  var currentBusinessDoc: Option[Document] = None

  index

  println(s"${reviewWriter.numDocs} review documents indexed!")
  println(s"${businessWriter.numDocs} business documents indexed!")

  db.close
  reviewWriter.close
  businessWriter.close

  println("Import done.")

  def index() {
    val pb = new ProgressBar("Importing", db.reviewCount + 1)
    val rs = db.reviewsWithBusinessInfo
    while (rs.next) {
      indexReviewDocumentFromRow(rs)
      indexBusinessDocumentFromRow(rs)

      pb.step
    }

    // one leftover document
    saveCurrentBusinessDocument
    pb.step

    pb.close
  }

  def indexReviewDocumentFromRow(rs: ResultSet) {
    reviewWriter.addDocument(makeReviewDocument(
      businessName = rs.getString("business.name"),
      text = rs.getString("review.text"),
      date = rs.getDate("review.date").getTime(),
      useful = rs.getLong("review.useful")
    ))
  }

  def indexBusinessDocumentFromRow(rs: ResultSet) {
    val businessId = rs.getString("business.id")
    val previousBusinessId = currentBusinessDoc match {
      case Some(doc) => Some(doc.getField("business.id").stringValue)
      case None => None
    }
    if (!previousBusinessId.isDefined || businessId != previousBusinessId.get) {
      saveCurrentBusinessDocument

      val formattedLocation = List("business.neighborhood", "business.address",
        "business.city", "business.state", "business.postal_code")
          .map(rs.getString(_))
          .filter(_.length > 0)
          .mkString(",")
      currentBusinessDoc = Some(makeBusinessDocument(
        id = businessId,
        name = rs.getString("business.name"),
        stars = rs.getFloat("business.stars"),
        latitude = rs.getFloat("business.latitude"),
        longitude = rs.getFloat("business.longitude"),
        formattedLocation = formattedLocation
      ))
    }

    val text = rs.getString("review.text")
    currentBusinessDoc.get.add(new TextField("business.reviewText", text, Field.Store.YES))
  }

  def saveCurrentBusinessDocument() {
    currentBusinessDoc match {
      case Some(doc) => {
        val reviewCount = doc.getValues("business.reviewText").length
        doc.add(new NumericDocValuesField("business.reviewCount", reviewCount))
        doc.add(new StoredField("business.reviewCount", reviewCount))
        businessWriter.addDocument(doc)
      }
      case None => {}
    }
  }
}
