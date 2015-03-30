package com.omnius.watcheye.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogMetricRepository implements MetricRepository {

  private static final Logger LOG = LoggerFactory.getLogger(LogMetricRepository.class);

  @Override
  public void saveMetric(Metric metric) {
    LOG.info("Metric saved {}", metric);
  }

}
