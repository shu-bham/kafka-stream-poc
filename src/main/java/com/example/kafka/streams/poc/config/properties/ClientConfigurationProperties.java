package com.example.kafka.streams.poc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "client.config")
@Data
public class ClientConfigurationProperties {

  private int connectTimeout;
  private int receiveTimeout;
  private String baseUrl;
  private String cacertsPath;
}
