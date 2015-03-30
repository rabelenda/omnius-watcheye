package com.omnius.watcheye.domain.collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import com.google.common.io.CharStreams;
import com.omnius.watcheye.OmniusWatcheyeApplication;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@Configuration
@SpringApplicationConfiguration(classes = OmniusWatcheyeApplication.class)
public class JmxMetricCollectorJsonSerializationIT {

  @Autowired
  protected MappingJackson2HttpMessageConverter converter;

  @Value("jmxMetricCollector.json")
  private Resource jmxMetricCollectorJsonFile;

  private JmxMetricCollector jmxCollector;

  @Before
  public void setup() {
    jmxCollector = new JmxMetricCollector();
    jmxCollector.setMetricName("test");
    jmxCollector.setPollPeriod(Duration.ofSeconds(5));
    jmxCollector.setHost("localhost");
    jmxCollector.setPort(2099);
    jmxCollector.setMbeanPath("com.omnius.watcheye.test:service=testBean");
    jmxCollector.setMbeanAttribute("SimpleAttribute");
  }

  @Test
  public void shouldGetJmxMetricControllerWhenLoadFromJson()
      throws IOException {
    MetricCollector loadedCollector = converter.getObjectMapper()
        .readValue(jmxMetricCollectorJsonFile.getInputStream(), MetricCollector.class);
    assertThat(loadedCollector, is(jmxCollector));
  }

  @Test
  public void shouldGetExpectedJsonWhenWritingToJson() throws IOException, JSONException {
    String expectedJson = getJsonStringFromResource(jmxMetricCollectorJsonFile);
    String actualJson = converter.getObjectMapper().writeValueAsString(jmxCollector);
    JSONAssert.assertEquals(expectedJson, actualJson, true);
  }

  private String getJsonStringFromResource(Resource r) throws IOException {
    try (InputStreamReader reader = new InputStreamReader(r.getInputStream())) {
      return CharStreams.toString(reader);
    }
  }

}
