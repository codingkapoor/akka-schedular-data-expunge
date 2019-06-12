
name := "akka-schedular-data-expunge"

version := "0.1"

scalaVersion := "2.12.8"

resolvers ++= Seq("restlet" at "http://maven.restlet.org")

lazy val akkaVersion = "2.5.23"
lazy val akkaHttpVersion = "10.1.8"
lazy val logbackVersion = "1.2.3"

lazy val akka = "com.typesafe.akka" %% "akka-actor" % akkaVersion withSources()
lazy val akkaHttpExp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
lazy val akkaHttpSpray = "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
lazy val akkaStreamTest = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
lazy val akkaHttpXml = "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion
lazy val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test"
lazy val akkaHttpTestKit = "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test"
lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.1" % "test"
lazy val betterFiles = "com.github.pathikrit" %% "better-files-akka" % "3.6.0"
lazy val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.10.2"
lazy val logBackCore = "ch.qos.logback" % "logback-core" % logbackVersion
lazy val logBackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion

libraryDependencies ++= Seq(akka, akkaHttpExp, akkaHttpSpray, akkaStream, akkaStreamTest, akkaTestkit,
  akkaHttpTestKit, scalaTest, scalaLogging, akkaHttpXml, logBackCore, logBackClassic, betterFiles, pureConfig)

enablePlugins(JavaAppPackaging, UniversalPlugin)
