package ir

import java.util.Date
import org.apache.lucene.document.Document

case class ReviewHit(businessName: String, text: String, useful: Long, date: Date)

object ReviewHit {
  def fromDocument(doc: Document) = ReviewHit(
    businessName = doc.getField("review.business.name").stringValue,
    text = doc.getField("review.text").stringValue,
    useful = doc.getField("review.useful").numericValue.longValue,
    date = new Date(doc.getField("review.date").numericValue.longValue)
  )
}
