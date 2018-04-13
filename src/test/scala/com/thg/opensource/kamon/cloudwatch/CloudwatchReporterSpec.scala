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

import java.time.Instant

import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsync
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest
import kamon.metric._
import org.mockito.Matchers
import org.scalatest.FlatSpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.{times, verify}

class CloudwatchReporterSpec extends FlatSpec with MockitoSugar {

  private val config = CloudwatchReporter.Configuration(namespace = "test")

  it should "interact with cloudwatchclient when metrics" in {
    val cloudwatchClient = mock[AmazonCloudWatchAsync]
    val reporter = new CloudwatchReporter(cloudwatchClient, config)
    val metric = MetricValue("test-metrics",
      Map.empty[String,String],
      new MeasurementUnit(MeasurementUnit.Dimension.Time, MeasurementUnit.Magnitude("a",1.0)),
      1l
    )
    val periodSnapshot = PeriodSnapshot(Instant.now(),
      Instant.now(),
      MetricsSnapshot(Seq.empty[MetricDistribution],
        Seq.empty[MetricDistribution],
        Seq.empty[MetricValue],
        Seq(metric)
      )
    )
    reporter.reportPeriodSnapshot(periodSnapshot)

    verify(cloudwatchClient, times(1)).putMetricDataAsync(Matchers.any[PutMetricDataRequest])
  }

  it should "not interact with cloudwatchclient when no metrics" in {
    val cloudwatchClient = mock[AmazonCloudWatchAsync]
    val reporter = new CloudwatchReporter(cloudwatchClient, config)
    val periodSnapshot = PeriodSnapshot(Instant.now(),
      Instant.now(),
      MetricsSnapshot(Seq.empty[MetricDistribution],
        Seq.empty[MetricDistribution],
        Seq.empty[MetricValue],
        Seq.empty[MetricValue]
      )
    )
    reporter.reportPeriodSnapshot(periodSnapshot)

    verify(cloudwatchClient, times(0)).putMetricDataAsync(Matchers.any[PutMetricDataRequest])
  }

}
