package com.omnius.watcheye.domain;

import com.omnius.watcheye.domain.collectors.MetricCollector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class InMemoryMetricCollectorRepository implements MetricCollectorRepository {

  private final Map<String, MetricCollector> metricCollectors = new HashMap<>();

  @Override
  public void addMetricCollector(MetricCollector metricCollector) {
    metricCollectors.put(metricCollector.getMetricName(), metricCollector);
  }

  @Override
  public Optional<MetricCollector> getMetricCollector(String name) {
    return Optional.ofNullable(metricCollectors.get(name));
  }

  @Override
  public List<MetricCollector> getMetricCollectors() {
    return new ArrayList<>(metricCollectors.values());
  }
}
