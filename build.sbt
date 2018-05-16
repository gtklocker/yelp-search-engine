import Dependencies._

lazy val root = (project in file("."))
  .enablePlugins(SbtTwirl, ScalatraPlugin)
  .settings(
    inThisBuild(List(
      organization := "com.github.gtklocker",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
    )),

    name := "irclass",

    resolvers += Classpaths.typesafeReleases,

    libraryDependencies += scalaTest % Test,
    libraryDependencies ++= luceneDependencies,
    libraryDependencies ++= mysqlDependencies,
    libraryDependencies ++= scalatraDependencies,
    libraryDependencies ++= progressBarDependencies
  )
