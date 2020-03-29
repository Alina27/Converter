name := "CurrencyConverter"

version := "0.1"

scalaVersion := "2.12.7"

lazy val akkaHttpVersion = "10.1.10"
lazy val akkaVersion    = "2.5.25"

libraryDependencies ++=  Seq (
  "io.argonaut" %% "argonaut" % "6.2.1",
  "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
  "org.scalaz" %% "scalaz-core" % "7.2.29",
  "ch.megard" %% "akka-http-cors" % "0.3.1",
)
