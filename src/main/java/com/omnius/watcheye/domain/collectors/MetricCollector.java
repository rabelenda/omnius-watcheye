package com.omnius.watcheye.domain.collectors;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.annotations.VisibleForTesting;
import com.omnius.watcheye.domain.Metric;
import com.omnius.watcheye.domain.MetricRepository;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = JmxMetricCollector.class, name = "jmx")})
public abstract class MetricCollector {

  protected String metricName;
  protected Duration pollPeriod;
  protected MetricRepository repository;
  private ScheduledExecutorService executorService;
  private Clock clock = Clock.systemUTC();

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  @SuppressWarnings("UnusedDeclaration")
  public Duration getPollPeriod() {
    return pollPeriod;
  }

  public void setPollPeriod(Duration pollPeriod) {
    this.pollPeriod = pollPeriod;
  }

  @JsonIgnore
  public void setRepository(MetricRepository repository) {
    this.repository = repository;
  }

  @JsonIgnore
  public void setExecutorService(ScheduledExecutorService executorService) {
    this.executorService = executorService;
  }

  @JsonIgnore
  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }

  public void start() {
    checkArgument(!metricName.isEmpty(), "Empty metricName");
    connect();
    executorService
        .scheduleAtFixedRate(this::collect, 0, pollPeriod.toMillis(), TimeUnit.MILLISECONDS);
  }

  protected abstract void connect();

  protected abstract void collect();

  protected Metric buildMetric(Object value)
      throws MBeanException, AttributeNotFoundException, InstanceNotFoundException,
             ReflectionException, IOException {
    return new Metric(Instant.now(clock), metricName, value);
  }

}
