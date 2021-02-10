package com.example.kafka.streams.poc.client.impl;

import com.example.kafka.streams.poc.client.ServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class ServiceClientImpl implements ServiceClient {

  private final WebClient webClient;

  public ServiceClientImpl(@Qualifier("myWebClient") WebClient webClient) {
    this.webClient = webClient;
  }

  @Override
  public Mono<Boolean> isValidUser(String id, String name) {
    String path = "/path/{id}";
    return webClient
        .get()
        .uri(uriBuilder -> uriBuilder.path(path).queryParam("name", name).build(id))
        .retrieve()
        .bodyToMono(Boolean.class);
  }
}
