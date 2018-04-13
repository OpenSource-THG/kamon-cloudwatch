## Build status
[![Build Status](https://travis-ci.org/OpenSource-THG/kamon-cloudwatch.svg?branch=master)](https://travis-ci.org/OpenSource-THG/kamon-cloudwatch)

## SBT dependency
Add the following to `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.amazonaws" % "aws-java-sdk" % "1.11.312",

  "io.kamon" %% "kamon-core" % kamonVersion,
  "io.kamon" %% "kamon-akka-2.5" % "1.0.1",
  "com.thg.opensource.kamon.cloudwatch" %% "kamon-cloudwatch" % "0.0.1"
)
```

## SBT Plugin configuration
Add the following to `plugins.sbt`:

```scala
resolvers += Resolver.bintrayRepo("kamon-io", "sbt-plugins")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % "1.1.0")
```

## Cloudwatch configuration
Add to `application.conf`:
```
  kamon {
    reporters = ["com.thg.opensource.kamon.cloudwatch.CloudwatchReporter"]
  
    cloudwatch {
        namespace = "<your>/<cloudwatch namespace>"
    }
    
    modules {
      kamon-system-metrics.auto-start = yes
      kamon-scala {
        requires-aspectj = yes
        auto-start = yes
      }
      kamon-akka {
        auto-start = yes
        requires-aspectj = yes
        extension-id = "kamon.akka.Akka"
      }
    }

    kamon.util.filters {
      "akka.tracked-actor" {
        includes = [ "**" ]
      }

      "akka.tracked-dispatcher" {
        includes = [ "**" ]
      }

      "akka.traced-actor" {
        includes = [ "**" ]
      }

      "test" {
        includes = [ "**" ]
      }
    }
  }
```