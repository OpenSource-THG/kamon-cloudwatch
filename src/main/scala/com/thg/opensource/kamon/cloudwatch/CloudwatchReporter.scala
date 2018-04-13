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

import com.amazonaws.services.cloudwatch.model.{MetricDatum, PutMetricDataRequest, StatisticSet}
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync
import com.typesafe.config.Config
import kamon.metric._
import kamon.{Kamon, MetricReporter}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object CloudwatchReporter {
  case class Configuration(namespace: String)

  def apply(amazonCloudWatch: AmazonCloudWatchAsync, cfg: CloudwatchReporter.Configuration) = new CloudwatchReporter(amazonCloudWatch, cfg)
}

class CloudwatchReporter(amazonCloudWatch: AmazonCloudWatchAsync, var config: CloudwatchReporter.Configuration) extends MetricReporter {

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

    current.metrics.counters.foreach(m => {
      logger.debug(s"${m.name} => ${m.value}")
      val putMetricDataRequest = new PutMetricDataRequest()
      putMetricDataRequest.setNamespace(config.namespace)
      putMetricDataRequest.setMetricData(createMetricDatum(m))
      try {
        amazonCloudWatch.putMetricDataAsync(putMetricDataRequest)
      } catch {
        case e: Exception =>
          logger.error(s"Unable to push metric to cloudwatch: ${e.getMessage}")
      }
    })

    current.metrics.rangeSamplers.foreach(s => {
      logger.debug(s"${s.name}")
      val putMetricDataRequest = new PutMetricDataRequest()
      putMetricDataRequest.setNamespace(config.namespace)
      putMetricDataRequest.setMetricData(createStatisticsSet(s))
      try {
        amazonCloudWatch.putMetricDataAsync(putMetricDataRequest)
      } catch {
        case e: Exception =>
          logger.error(s"Unable to push statistic set to cloudwatch: ${e.getMessage}")
      }
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

  private def createStatisticsSet(md: MetricDistribution): util.Collection[MetricDatum] = {
    val ss = new StatisticSet()
      .withMaximum(md.distribution.max.toDouble)
      .withMinimum(md.distribution.min.toDouble)
      .withSampleCount(md.distribution.count.toDouble)
      .withSum(md.distribution.sum.toDouble)

    val d = new MetricDatum()
      .withMetricName(md.name)
      .withStatisticValues(ss)

    List(d).asJavaCollection
  }

}
