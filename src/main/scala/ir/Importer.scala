package ir

import DocumentMakers.{makeReviewDocument, makeBusinessDocument}

import org.apache.lucene.document.DateTools.Resolution
import org.apache.lucene.document.{Document, TextField, StringField,
LatLonPoint, Field, StoredField, NumericDocValuesField, FloatDocValuesField,
DateTools}
import me.tongfei.progressbar.ProgressBar
import java.sql.ResultSet

object Importer extends App {
  println("Starting import... to %s".format(Lucene.indexPath.toString()))

  val writer = Lucene.writer
  val db = new Database
  var currentBusinessDoc: Option[Document] = None

  index

  println(s"${writer.numDocs} document(s) indexed!")

  db.close
  writer.close

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
    writer.addDocument(makeReviewDocument(
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
      currentBusinessDoc = Some(makeBusinessDocument(
        id = businessId,
        name = rs.getString("business.name"),
        stars = rs.getFloat("business.stars"),
        latitude = rs.getFloat("business.latitude"),
        longitude = rs.getFloat("business.longitude")
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
        writer.addDocument(doc)
      }
      case None => {}
    }
  }
}
