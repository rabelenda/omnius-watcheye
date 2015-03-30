package com.omnius.watcheye.domain.collectors;

import com.google.common.base.MoreObjects;
import com.omnius.watcheye.domain.Metric;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.Objects;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxMetricCollector extends MetricCollector {

  private static final Logger LOG = LoggerFactory.getLogger(JmxMetricCollector.class);

  private String host;
  private int port;
  private String mbeanPath;
  private String mbeanAttribute;

  private JMXConnector jmxConnector;
  private MBeanServerConnection jmxConnection;
  private ObjectName jmxMbeanName;

  @SuppressWarnings("UnusedDeclaration")
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @SuppressWarnings("UnusedDeclaration")
  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getMbeanAttribute() {
    return mbeanAttribute;
  }

  public void setMbeanAttribute(String mbeanAttribute) {
    this.mbeanAttribute = mbeanAttribute;
  }

  @SuppressWarnings("UnusedDeclaration")
  public String getMbeanPath() {
    return mbeanPath;
  }

  public void setMbeanPath(String mbeanPath) {
    this.mbeanPath = mbeanPath;
  }

  @Override
  protected void connect() {
    JMXServiceURL jmxUrl = getValidatedJmxUrl();
    jmxMbeanName = getValidatedMbeanName();
    jmxConnector = getValidatedConnector(jmxUrl);
    try {
      jmxConnection = getValidatedJmxConnection(jmxConnector);
      validateGettingAttribute(jmxMbeanName, jmxConnection);
    } catch (Exception e) {
      try {
        jmxConnector.close();
      } catch (IOException e1) {
        e.addSuppressed(e1);
      }
      throw e;
    }
  }

  private JMXServiceURL getValidatedJmxUrl() {
    try {
      return new JMXServiceURL(buildUrl());
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid url format for '" + buildUrl() + "'", e);
    }
  }

  private String buildUrl() {
    return String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port);
  }

  private ObjectName getValidatedMbeanName() {
    try {
      return new ObjectName(mbeanPath);
    } catch (MalformedObjectNameException e) {
      throw new IllegalArgumentException("Invalid mbean path format for '" + mbeanPath + "'", e);
    }
  }

  private JMXConnector getValidatedConnector(JMXServiceURL jmxUrl) {
    try {
      return JMXConnectorFactory.connect(jmxUrl, null);
    } catch (IOException e) {
      throw connectionException(e);
    }
  }

  private IllegalArgumentException connectionException(IOException e) {
    return new IllegalArgumentException("Can't connect to '" + buildUrl() + "'", e);
  }

  private MBeanServerConnection getValidatedJmxConnection(JMXConnector jmxConnector) {
    try {
      return jmxConnector.getMBeanServerConnection();
    } catch (IOException e) {
      throw connectionException(e);
    }
  }

  private void validateGettingAttribute(ObjectName jmxMBeanName,
                                        MBeanServerConnection jmxConnection) {
    try {
      jmxConnection.getAttribute(jmxMBeanName, mbeanAttribute);
    } catch (MBeanException | AttributeNotFoundException | ReflectionException |
        InstanceNotFoundException | IOException e) {
      throw new IllegalArgumentException(
          "Can't get the given attribute '" + mbeanAttribute + "' + from mbean '" + mbeanPath
          + "' from '" + buildUrl() + "'", e);
    }
  }

  @Override
  protected void collect() {
    Thread.currentThread().setName("JmxMetricCollector(" + metricName + ") at " + Instant.now());
    try {
      if (!isConnected()) {
        reconnect();
      }
      if (isConnected()) {
        Metric metric = buildMetric(jmxConnection.getAttribute(jmxMbeanName, mbeanAttribute));
        repository.saveMetric(metric);
      }
    } catch (MBeanException | AttributeNotFoundException | ReflectionException |
        InstanceNotFoundException | IOException e) {
      LOG.warn("Problem getting attribute '{}' from mbean '{}' from '{}'. Reconnecting...",
               mbeanPath,
               mbeanAttribute, buildUrl(), e);
      disconnect();
    }
  }

  private boolean isConnected() {
    return jmxConnection != null;
  }

  private void reconnect() {
    try {
      jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(buildUrl()), null);
      jmxConnection = jmxConnector.getMBeanServerConnection();
    } catch (IOException e) {
      LOG.warn("Problem reconnecting to '{}'.", buildUrl(), e);
    }
  }

  private void disconnect() {
    try {
      jmxConnector.close();
    } catch (IOException e) {
      LOG.warn("Problem closing existing connection to '{}'.", buildUrl(), e);
    }
    jmxConnection = null;
  }

  @Override
  public int hashCode() {
    return Objects
        .hash(metricName, pollPeriod, host, port, mbeanPath, mbeanAttribute, jmxConnector,
              jmxConnection, jmxMbeanName);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final JmxMetricCollector other = (JmxMetricCollector) obj;
    return Objects.equals(this.metricName, other.metricName)
           && Objects.equals(this.pollPeriod, other.pollPeriod)
           && Objects.equals(this.host, other.host)
           && Objects.equals(this.port, other.port)
           && Objects.equals(this.mbeanPath, other.mbeanPath)
           && Objects.equals(this.mbeanAttribute, other.mbeanAttribute)
           && Objects.equals(this.jmxConnector, other.jmxConnector)
           && Objects.equals(this.jmxConnection, other.jmxConnection)
           && Objects.equals(this.jmxMbeanName, other.jmxMbeanName);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("metricName", metricName)
        .add("pollPeriod", pollPeriod)
        .add("host", host)
        .add("port", port)
        .add("mbeanPath", mbeanPath)
        .add("mbeanAttribute", mbeanAttribute)
        .toString();
  }
}
