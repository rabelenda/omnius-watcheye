package com.omnius.watcheye;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@SpringBootApplication
@EnableConfigurationProperties(OmniusWatcheyeProperties.class)
public class OmniusWatcheyeApplication {

  public static void main(String[] args) {
    SpringApplication.run(OmniusWatcheyeApplication.class, args);
  }

  @Bean
  public ScheduledExecutorService metricCollectorsExecutorService(OmniusWatcheyeProperties props) {
    return Executors.newScheduledThreadPool(props.getCollectorsPoolSize());
  }

  /* since there is no way to set serializationInclusion through spring boot application.yml we
  need to define a custom object mapper, and due to this all other spring boot jackson settings
  are not able to be used through application.yml
    */
  @Bean
  public MappingJackson2HttpMessageConverter jacksonConverter() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.registerModule(new JSR310Module());
    return new MappingJackson2HttpMessageConverter(mapper);
  }

}
