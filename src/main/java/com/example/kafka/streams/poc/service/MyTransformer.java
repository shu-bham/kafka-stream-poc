package com.example.kafka.streams.poc.service;

import com.example.kafka.streams.poc.client.ServiceClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.ExceptionClassifierRetryPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MyTransformer implements Transformer<String, String, KeyValue<String, String>> {

  private ProcessorContext processorContext;
  private ServiceClient serviceClient;

  public MyTransformer(ServiceClient serviceClient) {
    this.serviceClient = serviceClient;
  }

  @Override
  public void init(ProcessorContext processorContext) {
    this.processorContext = processorContext;
  }

  @Override
  public KeyValue<String, String> transform(final String key, final String value) {
    // retry template for re-process the input
    // data in case of processing exception
    RetryTemplate retryTemplate = new RetryTemplate();
    RetryPolicy retryPolicy = new SimpleRetryPolicy(Integer.MAX_VALUE);
    FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
    backOffPolicy.setBackOffPeriod(10_000);
    ExceptionClassifierRetryPolicy exceptionClassifierRetryPolicy =
        new ExceptionClassifierRetryPolicy();
    Map<Class<? extends Throwable>, RetryPolicy> policyMap = new HashMap<>();
    // adding exception to be retried
    policyMap.put(InterruptedException.class, retryPolicy);
    exceptionClassifierRetryPolicy.setPolicyMap(policyMap);
    retryTemplate.setRetryPolicy(exceptionClassifierRetryPolicy);
    retryTemplate.setBackOffPolicy(backOffPolicy);
    return retryTemplate.execute(context -> transformInternal(key, value, context.getRetryCount()));
  }

  @SneakyThrows
  private KeyValue<String, String> transformInternal(
      final String key, final String value, final int attempt) {
    Instant start = Instant.now();
    Instant end;
    Long offset = null;
    int partition = -1;
    try {
      log.info("Message Received key: {}, retry count: {}, value: {}", key, attempt, value);
      offset = processorContext.offset();
      partition = processorContext.partition();

      /** ADD PROCESSING LOGIC HERE */
      // ex: setting header "name" in the claim
      processorContext.headers().add("name", value.getBytes(StandardCharsets.UTF_8));

      String updatedVal = "default";
      if (serviceClient.isValidUser(key, value).block()) {
        updatedVal = value.toUpperCase();
      }
      // commit the offset and forward the message
      return new KeyValue<>(key, updatedVal);
    }
    //    catch (InterruptedException re) {
    //      log.warn("Retryable error on attempt: {} for key: {}", attempt, key, re);
    //      throw re;
    //    }
    catch (Exception e) {
      log.error("Uncaught error for key {}, ack but dont forward", key, e);
      // commit the offset and don't forward
      return null;
    } finally {
      end = Instant.now();
      log.debug("Claim Partition : {}, Offset : {}", partition, offset);
      log.info(
          "total processing time for claim: " + Duration.between(start, end).toMillis() + "ms");
    }
  }

  @Override
  public void close() {}
}
