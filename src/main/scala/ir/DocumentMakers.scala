package ir

import org.apache.lucene.document.{Document, TextField, StringField,
LatLonPoint, Field, StoredField, NumericDocValuesField, FloatDocValuesField,
DateTools}

object DocumentMakers {
  def makeReviewDocument(businessName: String, text: String, date: Long,
      useful: Long): Document = {
    def ns(fieldName: String) = "review." + fieldName

    val doc = new Document
    doc.add(new StringField(ns("business.name"), businessName, Field.Store.YES))
    doc.add(new TextField(ns("text"), text, Field.Store.YES))
    doc.add(new NumericDocValuesField(ns("date"), date))
    doc.add(new NumericDocValuesField(ns("useful"), useful))
    doc
  }

  def makeBusinessDocument(id: String, name: String, stars: Float, allReviews:
      String, reviewCount: Long, latitude: Double, longitude: Double): Document = {
    def ns(fieldName: String) = "business." + fieldName

    val doc = new Document
    doc.add(new StringField(ns("id"), id, Field.Store.YES))
    doc.add(new StringField(ns("name"), name, Field.Store.YES))
    doc.add(new StoredField(ns("stars"), stars))
    doc.add(new FloatDocValuesField(ns("stars"), stars))
    doc.add(new TextField(ns("allReviews"), allReviews, Field.Store.YES))
    doc.add(new NumericDocValuesField(ns("reviewCount"), reviewCount))
    doc.add(new StoredField(ns("reviewCount"), reviewCount))
    doc.add(new LatLonPoint(ns("location"), latitude, longitude))
    doc
  }
}
