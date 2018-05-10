package ir

import org.apache.lucene.document.Document

class BusinessHit(val id: String, val name: String, val allReviews: String)

object BusinessHit {
  def fromDocument(doc: Document) = new BusinessHit(
    id = doc.getField("business.id").stringValue,
    name = doc.getField("business.name").stringValue,
    allReviews = doc.getField("business.allReviews").stringValue,
  )
}
