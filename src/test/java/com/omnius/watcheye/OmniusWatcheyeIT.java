package com.omnius.watcheye;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.io.CharStreams;
import com.omnius.watcheye.util.JmxServerTestRule;
import java.io.FileReader;
import java.io.IOException;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = OmniusWatcheyeApplication.class)
@WebAppConfiguration
@IntegrationTest({"server.port=0"})
public class OmniusWatcheyeIT {

  public static final String OMNIUS_WATCHEYE_COLLECTORS_PATH = "/omnius-watcheye/collectors";

  @Value("${local.server.port}")
  int appPort;

  @Value("${security.user.name}")
  String username;

  @Value("${security.user.password}")
  String password;

  private RestTemplate restTemplate;

  private String appUrl;

  @ClassRule
  public static JmxServerTestRule jmxServer = new JmxServerTestRule();

  @Before
  public void setup() {
    appUrl = "http://localhost:" + appPort;
    restTemplate = new TestRestTemplate(username, password);
  }

  @Test
  public void shouldCollectJmxMetricsWhenCreateJmxCollector()
      throws IOException, InterruptedException {
    ResponseEntity<String> response =
        createCollector(getJsonStringFromFile("/jmxMetricCollector.json"));

    assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
    //TODO remove sleep only left to see some long in repository, and use InfluxDB repository to check
    Thread.sleep(5000);
  }

  private String getJsonStringFromFile(String fileName) throws IOException {
    try (FileReader reader = new FileReader(getClass().getResource(fileName).getFile())) {
      return CharStreams.toString(reader);
    }
  }

  private ResponseEntity<String> createCollector(String collectorJson) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return restTemplate.postForEntity(appUrl + OMNIUS_WATCHEYE_COLLECTORS_PATH,
                                      new HttpEntity<>(collectorJson, headers),
                                      String.class);
  }

  @Test
  public void shouldGetBadRequestResponseWhenCreateJmxCollectorWithInvalidPort()
      throws IOException, InterruptedException {
    ResponseEntity<String> response =
        createCollector(getJsonStringFromFile("/jmxMetricCollectorWithInvalidPort.json"));

    assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
  }

}
