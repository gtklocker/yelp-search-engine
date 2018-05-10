package ir

import org.apache.lucene.document.Document

case class BusinessHit(id: String, name: String, allReviews: String)

object BusinessHit {
  def fromDocument(doc: Document) = BusinessHit(
    id = doc.getField("business.id").stringValue,
    name = doc.getField("business.name").stringValue,
    allReviews = doc.getField("business.allReviews").stringValue,
  )
}
