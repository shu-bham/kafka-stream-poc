package com.example.kafka.streams.poc.config;

import com.example.kafka.streams.poc.client.ServiceClient;
import com.example.kafka.streams.poc.service.MyTransformer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanCustomizer;

import java.util.function.Function;

@Slf4j
@Configuration
public class AppProcessor {
  private ServiceClient serviceClient;

  public AppProcessor(ServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  @Bean
  public Function<KStream<String, String>, KStream<String, String>> process() {

    return input -> input.transform(() -> new MyTransformer(serviceClient));
  }

  @Bean
  public StreamsBuilderFactoryBeanCustomizer streamsBuilderFactoryBeanCustomizer() {
    return factoryBean ->
        factoryBean.setKafkaStreamsCustomizer(
            kafkaStreams -> {
              kafkaStreams.setUncaughtExceptionHandler(
                  (t, e) -> {
                    log.error("Error occurred while Processing, shutting down: ", e);
                    System.exit(0);
                  });
            });
  }
}
