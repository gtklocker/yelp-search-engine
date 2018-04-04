import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

  lazy val luceneDependencies = Seq(
    "org.apache.lucene" % "lucene-core"  % "7.2.1",
    "org.apache.lucene" % "lucene-analyzers-common" % "7.2.1",
  )

  lazy val mysqlDependencies = Seq(
    "mysql" % "mysql-connector-java" % "5.1.24",
  )
}
