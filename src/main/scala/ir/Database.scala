package ir

import java.sql.{Connection, DriverManager, Statement, ResultSet}

class Database {
  Class.forName("com.mysql.jdbc.Driver")
  private val url = "jdbc:mysql://localhost:3306/yelp_db"
  private val username = "root"
  private val password = "root"
  private var connection = DriverManager.getConnection(url, username, password)

  def close() {
    connection.close
  }

  def queryResults(query: String): ResultSet = {
    val statement = createStreamingStatement(connection)
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
