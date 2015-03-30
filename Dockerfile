FROM dockerfile/java:oracle-java8

WORKDIR /opt/omnius-watcheye

COPY target/omnius-watcheye.jar /opt/omnius-watcheye/omnius-watcheye.jar

EXPOSE 8080
CMD ["java", "-jar", "omnius-watcheye.jar"]
