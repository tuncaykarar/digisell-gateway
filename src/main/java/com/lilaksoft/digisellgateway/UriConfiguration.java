package com.lilaksoft.digisellgateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties
class UriConfiguration {

  private String httpbin = "http://httpbin.org:80";

  private String coreApiUrl = "http://localhost:8080";

  public String getHttpbin() {
    return httpbin;
  }

  public String getCoreApiUrl() {
    return coreApiUrl;
  }

  public void setHttpbin(String httpbin) {
    this.httpbin = httpbin;
  }

  public void setCoreApiUrl(String coreApiUrl) {
    this.coreApiUrl = coreApiUrl;
  }
}