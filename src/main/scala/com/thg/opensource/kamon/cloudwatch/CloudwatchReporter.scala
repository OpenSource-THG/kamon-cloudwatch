package com.thg.opensource.kamon.cloudwatch

import kamon.MetricReporter

object CloudwatchReporter {
  case class Configuration(namespace: String)
}

class CloudwatchReporter extends MetricReporter {

}
