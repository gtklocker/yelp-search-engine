package ir

import java.sql.{Connection, DriverManager, Statement, ResultSet}

class Database {
  Class.forName("com.mysql.jdbc.Driver")
  private val url = "jdbc:mysql://localhost:3306/yelp_db"
  private val username = "root"
  private val password = "root"
  private var connection = DriverManager.getConnection(url, username, password)
  private val limit = sys.env.get("IR_DB_LIMIT")

  def reviewCount(): Long = {
    querySingleLong("select count(*) from review join business on review.business_id = business.id where city = 'Toronto'")
  }

  def businessCount(): Long = {
    querySingleLong("select count(*) from business where city = 'Toronto'")
  }

  def reviewsWithBusinessInfo(): ResultSet = {
    queryResults("""select business.name, review.text, review.date,
                   |review.useful, business.id, business.name, business.stars,
                   |business.latitude, business.longitude,
                   |business.neighborhood, business.address,
                   |business.city, business.state, business.postal_code
                   |from review join business
                   |on review.business_id = business.id
                   |where business.city = 'Toronto'
                   |order by business.id
                   """.stripMargin)
  }

  def querySingleLong(query: String): Long = {
    val res = queryResults(query)
    res.next
    val ret = res.getLong(1)
    while (res.next) {}
    ret
  }

  def close() {
    connection.close
  }

  def queryResults(query: String): ResultSet = {
    val statement = createStreamingStatement(connection)
    if (limit.isDefined) {
      statement.setMaxRows(limit.get.toInt)
    }
    statement.executeQuery(query)
  }

  private def createStreamingStatement(connection: Connection): Statement = {
    val statement =
      connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
        java.sql.ResultSet.CONCUR_READ_ONLY)
    statement.setFetchSize(Integer.MIN_VALUE)
    statement
  }
}
