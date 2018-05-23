package ir

import org.apache.lucene.document.Document

case class BusinessHit(id: String, name: String, reviewText: String, stars: Float, reviewCount: Long, latitude: Double, longitude: Double, formattedLocation: String)

object BusinessHit {
  def fromDocument(doc: Document) = BusinessHit(
    id = doc.getField("business.id").stringValue,
    name = doc.getField("business.name").stringValue,

    // actually only the first review is displayed
    reviewText = doc.getField("business.reviewText").stringValue,

    stars = doc.getField("business.stars").numericValue.floatValue,
    reviewCount = doc.getField("business.reviewCount").numericValue.longValue,

    latitude = doc.getField("business.latitude").numericValue.doubleValue,
    longitude = doc.getField("business.longitude").numericValue.doubleValue,

    formattedLocation = doc.getField("business.formattedLocation").stringValue
  )
}
