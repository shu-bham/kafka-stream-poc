package com.example.kafka.streams.poc.config;

import com.example.kafka.streams.poc.config.properties.ClientConfigurationProperties;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.SneakyThrows;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties({ClientConfigurationProperties.class})
public class ClientConfig {

  private final ClientConfigurationProperties clientConfigurationProperties;

  public ClientConfig(ClientConfigurationProperties clientConfigurationProperties) {
    this.clientConfigurationProperties = clientConfigurationProperties;
  }

  @SneakyThrows
  @Bean
  public WebClient myWebClient() {
    return WebClient.builder()
        .baseUrl(clientConfigurationProperties.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(HttpClient.from(getTimeOutClient())))
        .build();
  }

  private TcpClient getTimeOutClient()
      throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
//    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
//    trustStore.load(
//        new FileInputStream(ResourceUtils.getFile(clientConfigurationProperties.getCacertsPath())),
//        null);
//    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
//    trustManagerFactory.init(trustStore);
//    SslContext sslContext = SslContextBuilder.forClient().trustManager(trustManagerFactory).build();

    return TcpClient.create()
        .option(
            ChannelOption.CONNECT_TIMEOUT_MILLIS, clientConfigurationProperties.getConnectTimeout())
        .doOnConnected(
            c ->
                c.addHandlerLast(
                    new ReadTimeoutHandler(
                        (int)
                            Duration.ofMillis(clientConfigurationProperties.getReceiveTimeout())
                                .getSeconds())))
//        .secure(sslSpec -> sslSpec.sslContext(sslContext))
            ;
  }
}
