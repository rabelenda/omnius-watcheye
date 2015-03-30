package com.omnius.watcheye.web;

import com.omnius.watcheye.domain.MetricCollectorRepository;
import com.omnius.watcheye.domain.MetricRepository;
import com.omnius.watcheye.domain.collectors.MetricCollector;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/omnius-watcheye")
public class MetricCollectorController {

  private final MetricCollectorRepository collectorRepository;
  private final MetricRepository metricRepository;
  private final ScheduledExecutorService executorService;

  @Autowired
  public MetricCollectorController(MetricCollectorRepository collectorRepository,
                                   MetricRepository metricRepository,
                                   ScheduledExecutorService executorService) {
    this.collectorRepository = collectorRepository;
    this.metricRepository = metricRepository;
    this.executorService = executorService;
  }

  @RequestMapping(value = "/collectors", method = RequestMethod.POST)
  public ResponseEntity updateMetricCollector(@RequestBody MetricCollector metricCollector) {
    metricCollector.setRepository(metricRepository);
    metricCollector.setExecutorService(executorService);
    metricCollector.start();
    collectorRepository.addMetricCollector(metricCollector);
    URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{metricName}").buildAndExpand(metricCollector.getMetricName())
        .toUri();
    return ResponseEntity.created(location).build();
  }

  @ExceptionHandler(IllegalArgumentException.class)
  void handleBadRequests(HttpServletResponse response) throws IOException {
    response.sendError(HttpStatus.BAD_REQUEST.value());
  }

  @RequestMapping(value = "/collectors", method = RequestMethod.GET)
  public List<MetricCollector> getMetricCollectors() {
    return collectorRepository.getMetricCollectors();
  }

}
