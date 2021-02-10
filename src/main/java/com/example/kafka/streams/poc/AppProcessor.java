package com.example.kafka.streams.poc;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanCustomizer;

import java.util.function.Function;

@Slf4j
@Configuration
public class AppProcessor {

  @Bean
  public Function<KStream<String, String>, KStream<String, String>> process() {
    return input -> input.transform(MyTransformer::new);
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
