package com.omnius.watcheye;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "omnius-watcheye")
public class OmniusWatcheyeProperties {

  private int collectorsPoolSize;

  public int getCollectorsPoolSize() {
    return collectorsPoolSize;
  }

  public void setCollectorsPoolSize(int collectorsPoolSize) {
    this.collectorsPoolSize = collectorsPoolSize;
  }
}
