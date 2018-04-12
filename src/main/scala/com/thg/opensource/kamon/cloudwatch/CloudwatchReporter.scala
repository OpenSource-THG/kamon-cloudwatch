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

package com.thg.opensource.kamon.cloudwatch

import java.time.Duration
import java.util

import com.amazonaws.services.cloudwatch.model.{MetricDatum, PutMetricDataRequest}
import com.amazonaws.services.cloudwatch.{AmazonCloudWatch, AmazonCloudWatchClientBuilder}
import com.thg.opensource.kamon.cloudwatch.CloudwatchReporter.Configuration
import com.typesafe.config.Config
import kamon.metric.{MetricValue, PeriodSnapshot, PeriodSnapshotAccumulator}
import kamon.{Kamon, MetricReporter}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object CloudwatchReporter {
  case class Configuration(namespace: String)

  def apply(amazonCloudWatch: AmazonCloudWatch, cfg: CloudwatchReporter.Configuration) = new CloudwatchReporter(amazonCloudWatch, cfg)
}

class CloudwatchReporter(amazonCloudWatch: AmazonCloudWatch, var config: CloudwatchReporter.Configuration) extends MetricReporter {

  private val logger = LoggerFactory.getLogger(classOf[CloudwatchReporter])
  private val snapshotAccumulator = new PeriodSnapshotAccumulator(Duration.ofDays(365 * 5), Duration.ZERO)

  override def start() = {
    logger.info("CloudwatchReporter starting...")
    config = readConfiguration(Kamon.config)
  }

  override def stop() = {
    logger.info("CloudwatchReporter stopping...")
  }

  override def reconfigure(newConfig: Config) = {
    config = readConfiguration(newConfig)
  }

  override def reportPeriodSnapshot(snapshot: PeriodSnapshot) = {
    snapshotAccumulator.add(snapshot)
    val current = snapshotAccumulator.peek()
    val putMetricDataRequest: PutMetricDataRequest = new PutMetricDataRequest()

    putMetricDataRequest.setNamespace(config.namespace)

    current.metrics.counters.foreach(m => {
      logger.info(s"${m.name} => ${m.value}")
      putMetricDataRequest.setMetricData(createMetricDatum(m))
      amazonCloudWatch.putMetricData(putMetricDataRequest)
    })
  }

  private def readConfiguration(config: Config): CloudwatchReporter.Configuration = {
    val cloudwatchConfig = config.getConfig("kamon.cloudwatch")

    CloudwatchReporter.Configuration(
      namespace = cloudwatchConfig.getString("namespace")
    )
  }

  private def createMetricDatum(m: MetricValue): util.Collection[MetricDatum] = {
    val d = new MetricDatum()
      .withMetricName(m.name)
      .withValue(m.value.toDouble)

    List(d).asJavaCollection
  }

}
