package ir

import org.apache.lucene.document.Document

case class ReviewHit(businessName: String, text: String)

object ReviewHit {
  def fromDocument(doc: Document) = ReviewHit(
    businessName = doc.getField("review.business.name").stringValue,
    text = doc.getField("review.text").stringValue
  )
}
