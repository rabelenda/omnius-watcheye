package com.omnius.watcheye.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import java.beans.ConstructorProperties;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;

public class JmxServer {

  public static final int PORT = 2099;
  public static final String SIMPLE_ATTRIBUTE_VALUE = UUID.randomUUID().toString();
  public static final String COMPOSITE_INNER_ATTRIBUTE_VALUE = UUID.randomUUID().toString();
  private static final Duration STARTUP_THRESHOLD = Duration.ofSeconds(1);
  private static final Duration RUNTIME = Duration.ofMinutes(5);

  private Process server;

  public void start()
      throws IOException, InterruptedException {
    //Using plain System.out.println to avoid adding dependencies and have to use maven to run the class
    System.out.println("Starting JMX server on port " + PORT);
    String[] javaCmd = new String[]{"java", "-Dcom.sun.management.jmxremote.port=" + PORT,
                                    "-Dcom.sun.management.jmxremote.authenticate=false",
                                    "-Dcom.sun.management.jmxremote.ssl=false",
                                    getClass().getName(), SIMPLE_ATTRIBUTE_VALUE,
                                    COMPOSITE_INNER_ATTRIBUTE_VALUE};
    File workingDir = new File(getClass().getResource(File.separator).getFile());
    server = Runtime.getRuntime().exec(javaCmd, null, workingDir);
    if (server.waitFor(STARTUP_THRESHOLD.toMillis(), TimeUnit.MILLISECONDS)) {
      throw new IllegalStateException(new CommandResult(server).toString());
    }
  }

  private static class CommandResult {

    private final int exitCode;
    private final String output;
    private final String error;

    public CommandResult(Process process) throws IOException {
      exitCode = process.exitValue();
      output = CharStreams.toString(new InputStreamReader(process.getInputStream()));
      error = CharStreams.toString(new InputStreamReader(process.getErrorStream()));
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("exitCode", exitCode)
          .add("output", output)
          .add("error", error)
          .toString();
    }
  }

  public synchronized void stop() {
    //Using plain System.out.println to avoid adding dependencies and have to use maven to run the class
    System.out.println("Stopping JMX server on port " + PORT);
    server.destroyForcibly();
  }

  public static void main(String[] args) throws InterruptedException {
    registerMBean(new TestMBean(args[0], args[1]));
    Thread.sleep(RUNTIME.toMillis());
  }

  public static class TestMBean implements TestMXBean {

    public static final String PATH = "com.omnius.watcheye.test:service=testBean";
    public static final String SIMPLE_ATTRIBUTE_NAME = "SimpleAttribute";
    public static final String COMPOSITE_ATTRIBUTE_NAME = "CompositeAttribute";

    private final String simpleAttribute;
    private final CompositeTestType compositeAttribute;

    public TestMBean(String simpleAttribute, String compositeInnerAttribute) {
      this.simpleAttribute = simpleAttribute;
      this.compositeAttribute = new CompositeTestType(compositeInnerAttribute);
    }

    public String getSimpleAttribute() {
      return simpleAttribute;
    }

    public CompositeTestType getCompositeAttribute() {
      return compositeAttribute;
    }
  }

  @SuppressWarnings("UnusedDeclaration")
  public static interface TestMXBean {

    String getSimpleAttribute();

    CompositeTestType getCompositeAttribute();
  }

  public static class CompositeTestType {

    private final String innerAttr;

    @ConstructorProperties("innerAttr")
    public CompositeTestType(String innerAttr) {
      this.innerAttr = innerAttr;
    }

    public CompositeTestType(CompositeDataSupport value) {
      innerAttr = (String) value.get("innerAttr");
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getInnerAttr() {
      return innerAttr;
    }

    @Override
    public int hashCode() {
      return Objects.hash(innerAttr);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      final CompositeTestType other = (CompositeTestType) obj;
      return Objects.equals(this.innerAttr, other.innerAttr);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("innerAttr", innerAttr)
          .toString();
    }
  }

  private static void registerMBean(TestMBean testMbean) {
    MBeanServer ms = ManagementFactory.getPlatformMBeanServer();
    try {
      ms.registerMBean(testMbean, new ObjectName(TestMBean.PATH));
    } catch (InstanceAlreadyExistsException | MBeanRegistrationException |
        NotCompliantMBeanException | MalformedObjectNameException e) {
      Throwables.propagate(e);
    }
  }

}
