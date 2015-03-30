package com.omnius.watcheye.domain.collectors;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.omnius.watcheye.domain.Metric;
import com.omnius.watcheye.domain.MetricRepository;
import com.omnius.watcheye.util.JmxServer;
import com.omnius.watcheye.util.JmxServerTestRule;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.Executors;
import javax.management.openmbean.CompositeDataSupport;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JmxMetricCollectorJmxConnectionIT {

  private static final String METRIC_NAME = "test";
  private static final Duration POLL_PERIOD = Duration.ofSeconds(5);
  private static final Duration POLLING_START_THRESHOLD = Duration.ofSeconds(2);
  private static final Instant NOW = Instant.now();

  @ClassRule
  public static JmxServerTestRule jmxServer = new JmxServerTestRule();

  private JmxMetricCollector jmxCollector;

  @Mock
  MetricRepository repository;

  @Before
  public void setup() {
    jmxCollector = new JmxMetricCollector();
    jmxCollector.setMetricName(METRIC_NAME);
    jmxCollector.setPollPeriod(POLL_PERIOD);
    jmxCollector.setHost("localhost");
    jmxCollector.setPort(JmxServer.PORT);
    jmxCollector.setMbeanPath(JmxServer.TestMBean.PATH);
    jmxCollector.setMbeanAttribute(JmxServer.TestMBean.SIMPLE_ATTRIBUTE_NAME);
    jmxCollector.setRepository(repository);
    jmxCollector.setExecutorService(Executors.newSingleThreadScheduledExecutor());
    jmxCollector.setClock(Clock.fixed(NOW, ZoneId.systemDefault()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenStartWithInvalidPort() {
    jmxCollector.setPort(-1);
    jmxCollector.start();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenStartWithInvalidHostFormat() {
    jmxCollector.setHost("test");
    jmxCollector.start();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenStartWithNoJmxServerInUrl() {
    jmxCollector.setPort(80);
    jmxCollector.start();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenStartWithInvalidObjectNameFormat() {
    jmxCollector.setMbeanPath("test");
    jmxCollector.start();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenStartWithNonExistingObjectName() {
    jmxCollector.setMbeanPath(JmxServer.TestMBean.SIMPLE_ATTRIBUTE_NAME + "x");
    jmxCollector.start();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowIllegalArgumentExceptionWhenStartWithNonExistingMbeanAttribute() {
    jmxCollector.setMbeanAttribute("test");
    jmxCollector.start();
  }

  @Test
  public void shouldSendSimpleMetricToRepositoryWithCurrentDateWhenStartWithSimpleAttribute() {
    jmxCollector.start();
    verify(repository, timeout(POLLING_START_THRESHOLD.toMillis())).saveMetric(buildSimpleMetric());
  }

  private Metric buildSimpleMetric() {
    return new Metric(NOW, METRIC_NAME, JmxServer.SIMPLE_ATTRIBUTE_VALUE);
  }

  @Test
  public void shouldSendCompositeMetricToRepositoryWithCurrentDateWhenStartWithCompositeAttribute() {
    jmxCollector.setMbeanAttribute(JmxServer.TestMBean.COMPOSITE_ATTRIBUTE_NAME);
    jmxCollector.start();
    ArgumentCaptor<Metric> argument = ArgumentCaptor.forClass(Metric.class);
    verify(repository, timeout(POLLING_START_THRESHOLD.toMillis())).saveMetric(
        argument.capture());
    Metric metric = argument.getValue();
    assertThat(buildSimplifiedMetricFromCompositeOne(metric),
               is(new Metric(NOW, METRIC_NAME, new JmxServer.CompositeTestType(
                   JmxServer.COMPOSITE_INNER_ATTRIBUTE_VALUE))));
  }

  private Metric buildSimplifiedMetricFromCompositeOne(Metric metric) {
    return new Metric(metric.getTimestamp(), metric.getName(), new JmxServer.CompositeTestType(
        (CompositeDataSupport) metric.getValue()));
  }

  @Test
  public void shouldPollCollectingMetricsAfterStart() {
    jmxCollector.start();
    verify(repository,
           timeout(POLLING_START_THRESHOLD.plus(POLL_PERIOD.multipliedBy(2)).toMillis()).times(2))
        .saveMetric(buildSimpleMetric());
  }

  @Test
  public void shouldContinuePollingAfterConnectionIssueAfterStart()
      throws InterruptedException, IOException {
    jmxCollector.start();
    verify(repository, timeout(POLLING_START_THRESHOLD.toMillis())).saveMetric(buildSimpleMetric());
    jmxServer.stop();
    Mockito.reset(repository);
    Thread.sleep(POLL_PERIOD.toMillis());
    jmxServer.start();
    verify(repository, timeout(POLLING_START_THRESHOLD.plus(POLL_PERIOD).toMillis()))
        .saveMetric(buildSimpleMetric());
  }

}
