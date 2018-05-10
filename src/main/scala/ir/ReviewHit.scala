package ir

import org.apache.lucene.document.Document

class ReviewHit(val businessName: String, val text: String)

object ReviewHit {
  def fromDocument(doc: Document) = new ReviewHit(
    businessName = doc.getField("review.business.name").stringValue,
    text = doc.getField("review.text").stringValue
  )
}
