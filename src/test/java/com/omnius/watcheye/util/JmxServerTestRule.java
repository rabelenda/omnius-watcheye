package com.omnius.watcheye.util;

import java.io.IOException;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * Not implemented inside existing JmxServer because it would add additional dependencies to
 * JmxServer which would not allow a simple java command to run without specifying a correct
 * classpath. The easy way of specifying a classpath is by using mvn exec:exec but that adds a
 * considerable runtime penalty aside from indirection which makes harder to trace issues.
 */
public class JmxServerTestRule implements TestRule {

  private final JmxServer jmxServer = new JmxServer();

  @Override
  public Statement apply(Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        jmxServer.start();
        try {
          base.evaluate();
        } finally {
          jmxServer.stop();
        }
      }
    };
  }

  public void stop() {
    jmxServer.stop();
  }

  public void start() throws IOException, InterruptedException {
    jmxServer.start();
  }
}
