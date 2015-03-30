#omnius-watcheye

Is a RESTful micro service focused in collecting metrics from different sources () and store them in a time series database like influxdb or graphite for later charting and analysis.

This work started from the need of having such service for a bigger project (Omnius: still to be developed) and be able later on to chart such data. There are some existing tools that are similar like [jmxtrans](https://github.com/jmxtrans) but they don't fulfill my needs or are more complex that what I needed. The main idea is to have an easy way to get information from different sources and store it in a standardized way to be able later on to chart it and analyze in a central place.

##Building

###Build Preconditions
- JDK 1.8
- [Maven 3](http://maven.apache.org/)

This is a Maven project, so just running `mvn package` you would get
an executable jar in `target` folder.

If you want to run integration tests run `mvn verify`.

##Running

You can just run the application with `java -jar target/omnius-watcheye.jar` which will run current basic service.

###Docker

[Docker](https://www.docker.com/) allows to easily deploy an application to any environment properly isolating the process.

####Docker preconditions
- [Docker installed](https://docs.docker.com/installation/)

A `Dockerfile` is provided so if you want to build it (after running `mvn package`) you can do something like `docker build -t omnius-watcheye .` and then `docker run -p 8080:8080 omnius-watcheye` to run the container. If you use [boot2docker](http://boot2docker.io/) or [docker-machine](https://docs.docker.com/machine/) remember to use the docker ip (`boot2docker ip` or `docker-machine ip` will display it) instead of localhost from the host machine to access the API.

###docker-compose

Docker compose allows to easily run a set of containers, scale them, etc. I just use it for local environment startup and experimentation.

####Docker-compose preconditions
- [docker-compose installed](https://docs.docker.com/compose/install/)

A `docker-compose.yml` file is provided which allows to run `docker-compose up` and have the service running in its current basic form.
Additionally a `docker-compose-cadvisor-influxdb-graphana.yml` file is provided which allows through running `docker-compose -f docker-compose-cadvisor-influxdb-graphana.yml up` to experiment influxdb and graphana (currently used as a sandbox for experimentation).

##API
Currently the API is very limited and only allows to add JMX metric collector (without any storage of metrics).

An example of collecting JMX metrics for a local service:

```
POST http://localhost:8080/omnius-watcheye/collectors
Content-Type: application/json
Authorization: Basic YWRtaW46YWRtaW4=

{
  "type":"jmx",
  "metricName":"test",
  "pollPeriod":"PT5S",
  "host": "localhost",
  "port": 2099,
  "mbeanPath" : "com.omnius.watcheye.test:service=testBean",
  "mbeanAttribute" : "SimpleAttribute"
}
```

##Configuration

You can check `application.yml` and [spring-boot documentation](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle). Later on I will add [spring cloud](http://projects.spring.io/spring-cloud/) configuration service.

##Contributing

Please feel free to send pull requests or fork, or send questions and proposals as issues.

###Extending

The main point of extension is creating new `MetricCollector`. For this just extend the `MetricCollector` class, and add the type in `MetricCollector` class in `JsonSubTypes` annotation to let
[jackson](https://github.com/FasterXML/jackson) know how to distinguish the
JSON for the new collector from the others. If you need some singletons (bean),
could be for performance issues or just due to the logic required,
then add them to `OmniusWatcheyeApplication` or create a `@Configuration` class.

