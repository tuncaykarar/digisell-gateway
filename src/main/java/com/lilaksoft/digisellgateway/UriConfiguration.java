package com.lilaksoft.digisellgateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
class UriConfiguration {

  private String httpbin = "http://httpbin.org:80";

  public String getHttpbin() {
    return httpbin;
  }

  public void setHttpbin(String httpbin) {
    this.httpbin = httpbin;
  }
}