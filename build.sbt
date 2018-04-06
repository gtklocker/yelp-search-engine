import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.github.gtklocker",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "irclass",
    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= luceneDependencies,
    libraryDependencies ++= mysqlDependencies
  )
