package com.omnius.watcheye.domain;

import com.omnius.watcheye.domain.collectors.MetricCollector;
import java.util.List;
import java.util.Optional;

public interface MetricCollectorRepository {

  public void addMetricCollector(MetricCollector metricCollector);

  public Optional<MetricCollector> getMetricCollector(String name);

  public List<MetricCollector> getMetricCollectors();

}
