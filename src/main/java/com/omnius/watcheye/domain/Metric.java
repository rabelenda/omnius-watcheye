package com.omnius.watcheye.domain;

import com.google.common.base.MoreObjects;
import java.time.Instant;
import java.util.Objects;

public class Metric {

  private final Instant timestamp;
  private final String name;
  private final Object value;

  public Metric(Instant timestamp, String name, Object value) {
    this.timestamp = timestamp;
    this.name = name;
    this.value = value;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public String getName() {
    return name;
  }

  public Object getValue() {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(timestamp, name, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Metric other = (Metric) obj;
    return Objects.equals(this.timestamp, other.timestamp)
           && Objects.equals(this.name, other.name)
           && Objects.equals(this.value, other.value);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("timestamp", timestamp)
        .add("metricName", name)
        .add("value", value)
        .toString();
  }

}
