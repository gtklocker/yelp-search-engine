import sbt._

object Dependencies {
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.3"

  lazy val luceneDependencies = Seq(
    "org.apache.lucene" % "lucene-core"  % "7.3.0",
    "org.apache.lucene" % "lucene-analyzers-common" % "7.3.0",
    "org.apache.lucene" % "lucene-sandbox" % "7.3.0",
    "org.apache.lucene" % "lucene-queryparser" % "7.3.0",
  )

  lazy val mysqlDependencies = Seq(
    "mysql" % "mysql-connector-java" % "5.1.24",
  )

  val ScalatraVersion = "2.6.+"
  lazy val scalatraDependencies = Seq(
    "org.json4s"                  %% "json4s-jackson"      % "3.5.3",
    "org.scalatra"                %% "scalatra"            % ScalatraVersion,
    "org.scalatra"                %% "scalatra-scalate"    % ScalatraVersion,
    "org.scalatra"                %% "scalatra-specs2"     % ScalatraVersion    % Test,
    "org.scalatra"                %% "scalatra-atmosphere" % ScalatraVersion,
    "ch.qos.logback"              %  "logback-classic"     % "1.2.3"            % Provided,
    "org.eclipse.jetty"           %  "jetty-webapp"        % "9.4.7.v20170914"  % Provided,
    "javax.servlet"               %  "javax.servlet-api"   % "3.1.0"            % Provided
  )

  lazy val progressBarDependencies = Seq(
    "me.tongfei" % "progressbar" % "0.7.0"
  )
}
