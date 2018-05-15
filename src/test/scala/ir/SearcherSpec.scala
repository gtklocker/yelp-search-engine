package ir

import org.scalatest._

class SearcherSpec extends FlatSpec with Matchers {
  it should "not throw on business search sorted by stars" in {
    noException should be thrownBy Searcher.findBusinesses(Some("a"), None, sortedBy = Some(SortByStars))
  }

  it should "return business results sorted by stars in the correct order" in {
    val results = Searcher.findBusinesses(Some("a"), None, sortedBy = Some(SortByStars))
    results.map(_.stars) shouldBe sorted
  }

  it should "return business results sorted by review count in the correct order" in {
    val results = Searcher.findBusinesses(Some("a"), None, sortedBy = Some(SortByReviewCount))
    results.map(_.reviewCount) shouldBe sorted
  }

  it should "not throw on business search sorted by review count" in {
    noException should be thrownBy Searcher.findBusinesses(Some("a"), None, sortedBy = Some(SortByReviewCount))
  }

  it should "return the maximum amount ot business results on something generic" in {
    val results = Searcher.findBusinesses(Some("a"), None, None)
    results.length should be (20)
  }

  it should "return no results on something that does not exist" in {
    val results = Searcher.findBusinesses(Some("gtklockerd03sn0t3x1st"), None, None)
    results.length should be (0)
  }
}
