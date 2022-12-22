import sbt.Defaults._

scalaVersion := "2.12.1"

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
  "ch.qos.logback" % "logback-classic" % logback_version % "provided"
)
val nettydependencies = Seq(
  "io.netty" % "netty-all" % netty_version,
  "com.jcraft" % "jzlib" % jzlib_version,
  "io.netty" % "netty-transport-native-epoll" % netty_version classifier "linux-x86_64"
) ++ commondependencies

val jettydependencies = Seq(
  "org.eclipse.jetty.websocket" % "javax-websocket-server-impl" % jetty_websocket_version
) ++ commondependencies

val testClientdependencies = Seq(
  "org.eclipse.jetty.websocket" % "javax-websocket-client-impl" % jetty_websocket_version,
  "javax.websocket" % "javax.websocket-api" % javax_websocket_version % "provided",
  "io.dropwizard.metrics" % "metrics-core" % metrics_version
) ++ commondependencies

lazy val root = (project in file("."))
  .settings(defaultSettings: _*)
  .aggregate(netty, jetty, testclient)

lazy val netty = (project in file("netty"))
  .enablePlugins(AssemblyPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(defaultSettings: _*)
  .settings(libraryDependencies ++= nettydependencies)

lazy val jetty = (project in file("jetty"))
  .enablePlugins(sbtassembly.AssemblyPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(defaultSettings: _*)
  .settings(libraryDependencies ++= jettydependencies)

lazy val testclient = (project in file("testclient"))
  .enablePlugins(sbtassembly.AssemblyPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(defaultSettings: _*)
  .settings(libraryDependencies ++= testClientdependencies)
  .settings(
    Compile / mainClass := Some("com.colobu.c1000k.testclient.AllMain")
  )

resolvers ++= Seq(
  "Typesafe" at "https://repo.typesafe.com/typesafe/releases/",
  "jgit-repo" at "https://download.eclipse.org/jgit/maven",
  "sbt-plugins" at "https://repo.scala-sbt.org/scalasbt/",
  "mvnrepository" at "https://mvnrepository.com/artifact/",
  "google" at "https://maven.google.com/",
  "Maven Central Server" at "https://repo1.maven.org/maven2/",
  "apache" at "https://repo.maven.apache.org/maven2/"
)

lazy val defaultSettings = coreDefaultSettings ++ Seq(
  organization := "com.colobu.c1000k",
  version := "0.1",
  externalResolvers := Seq(
    "Typesafe" at "https://repo.typesafe.com/typesafe/releases/",
    "Java.net Maven2 Repository" at "https://download.java.net/maven/2/"
  ),
  scalaVersion := "2.13.10",
  scalacOptions := Seq("-deprecation", "-feature"),
  externalResolvers := Resolver.combineDefaultResolvers(resolvers.value.toVector, mavenCentral = true),
  ThisBuild / assemblyMergeStrategy := {
    case "META-INF/io.netty.versions.properties" => MergeStrategy.first
    case PathList("module-info.class") => MergeStrategy.discard
    case x if x.endsWith("/module-info.class") => MergeStrategy.discard
    case x =>
      val oldStrategy = (ThisBuild / assemblyMergeStrategy).value
      oldStrategy(x)
  }
)
