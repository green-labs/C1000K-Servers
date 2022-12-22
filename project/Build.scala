import java.text.SimpleDateFormat
import java.util.Date

import com.typesafe.sbt.packager.archetypes.{AkkaAppPackaging, JavaAppPackaging}
import com.typesafe.sbt.packager.universal.UniversalDeployPlugin
import sbt.Defaults._
import sbt.Keys._
import sbt._
import sbtassembly.MergeStrategy
import sbtbuildinfo.Plugin.{BuildInfoKey, _}
import sbtrelease.ReleasePlugin._
import sbtassembly.AssemblyKeys._


object Build extends sbt.Build {
  val netty_version = "4.1.86.Final"
  val jzlib_version = "1.1.3"
  val typesafe_config_version = "1.4.2"
  val jetty_websocket_version = "9.4.50.v20221201"
  val javax_websocket_version = "1.1"
  val metrics_version = "3.1.2"
  val scala_logging_version = "3.9.5"
  val logback_version = "1.4.5"

  val commondependencies = Seq(
    "com.typesafe" % "config" % typesafe_config_version,
    "com.typesafe.scala-logging" %% "scala-logging" % scala_logging_version,
    "ch.qos.logback" % "logback-classic" % logback_version
  )
  val nettydependencies = Seq(
    "io.netty" % "netty-all" % netty_version,
    "com.jcraft" % "jzlib" % jzlib_version,
    "io.netty" % "netty-transport-native-epoll" % netty_version classifier "linux-x86_64"
  ) ++ commondependencies

  val jettydependencies = Seq(
    "org.eclipse.jetty.websocket" % "javax-websocket-server-impl" % jetty_websocket_version
  ) ++ commondependencies

  lazy val testClientdependencies = Seq(
    "org.eclipse.jetty.websocket" % "javax-websocket-client-impl" % jetty_websocket_version,
    "javax.websocket" % "javax.websocket-api" % javax_websocket_version,
    "io.dropwizard.metrics" % "metrics-core" % metrics_version
  ) ++ commondependencies

  lazy val root = Project("c1000k", file("."))
    .settings(defaultSettings: _*)
    .aggregate(netty, jetty, testclient)

  lazy val netty = Project("netty", file("netty"))
    .enablePlugins(sbtassembly.AssemblyPlugin)
    .enablePlugins(JavaAppPackaging)
    .settings(defaultSettings: _*)
    .settings(libraryDependencies ++= nettydependencies)

  lazy val jetty = Project("jetty", file("jetty"))
    .enablePlugins(sbtassembly.AssemblyPlugin)
    .enablePlugins(JavaAppPackaging)
    .settings(defaultSettings: _*)
    .settings(libraryDependencies ++= jettydependencies)

  lazy val testclient = Project("testclient", file("testclient"))
    .enablePlugins(sbtassembly.AssemblyPlugin)
    .enablePlugins(JavaAppPackaging)
    .settings(defaultSettings: _*)
    .settings(libraryDependencies ++= testClientdependencies)
    .settings(mainClass in Compile := Some("com.colobu.c1000k.testclient.AllMain"))

  lazy val defaultSettings = coreDefaultSettings ++ releaseSettings ++ Seq(
    organization := "com.colobu.c1000k",
    version := "0.1",
    externalResolvers := Resolvers.all,
    scalaVersion := "2.13.10",
    scalacOptions := Seq("-deprecation", "-feature"),
    assemblyMergeStrategy in assembly := {
      case "META-INF/io.netty.versions.properties" => MergeStrategy.first
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    }
  )

  object Resolvers {
    val jgitrepo = "jgit-repo" at "http://download.eclipse.org/jgit/maven"
    val sbtPlugins = "sbt-plugins" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/"
    val mvnrepository = "mvnrepository" at "http://mvnrepository.com/artifact/"
    val google = "google" at "https://maven.google.com/"
    val maven2 = "maven2" at "https://repo1.maven.org/maven2/"
    val apacheMaven = "apache" at "https://repo.maven.apache.org/maven2/"

    val all = Seq(apacheMaven, mvnrepository, maven2, google, jgitrepo, sbtPlugins)
  }

}