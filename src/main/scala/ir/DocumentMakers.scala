package ir

import org.apache.lucene.document.{Document, TextField, StringField,
LatLonDocValuesField, Field, FloatDocValuesField, NumericDocValuesField,
DateTools}

object DocumentMakers {
  def makeReviewDocument(businessName: String, reviewText: String, date: Long,
      useful: Long): Document = {
    val doc = new Document
    doc.add(new StringField("business_name", businessName, Field.Store.YES))
    doc.add(new TextField("review_text", reviewText, Field.Store.YES))
    doc.add(new NumericDocValuesField("date", date))
    doc.add(new NumericDocValuesField("useful", useful))
    doc
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
}
