name := "kamon-cloudwatch"

val version = "0.1"
val scalaVersion = "2.12.5"
//val akkaVersion = "2.5.3"
val kamonVersion = "1.1.0"

libraryDependencies ++= Seq(
  //"com.typesafe.akka" %% "akka-actor" % akkaVersion,
  //"com.typesafe.akka" %% "akka-testkit" % akkaVersion,
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "com.amazonaws" % "aws-java-sdk" % "1.11.312",

  //"io.kamon" %% "kamon-core" % kamonVersion,
  //"io.kamon" %% "kamon-logback" % "1.0.0",
  "io.kamon" %% "kamon-akka-2.5" % "1.0.1",
  //"io.kamon" %% "kamon-prometheus" % "1.0.0"

)

resolvers += Resolver.bintrayRepo("kamon-io", "snapshots")