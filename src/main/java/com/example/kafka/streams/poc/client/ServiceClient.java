package com.example.kafka.streams.poc.client;

import reactor.core.publisher.Mono;

public interface ServiceClient {

  Mono<Boolean> isValidUser(final String id, final String name);
}
