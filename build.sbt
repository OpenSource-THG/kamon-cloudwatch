/**
  * Copyright 2018 THG
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

name := "kamon-cloudwatch"

val version = "0.0.1"
val scalaVersion = "2.12.5"
val kamonVersion = "1.1.0"

bintrayOrganization := Some("opensource-thg")
licenses := List(
  ("Apache-2.0",
    url("https://www.apache.org/licenses/LICENSE-2.0"))
)
bintrayRepository := "opensource-THG"
bintrayPackage := "kamon-cloudwatch"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.11.312",
  "io.kamon" %% "kamon-core" % kamonVersion,

  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-all" % "1.8.4"
)

resolvers += Resolver.bintrayRepo("kamon-io", "snapshots")