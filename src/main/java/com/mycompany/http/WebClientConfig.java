package com.mycompany.http;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

  @Bean
  public WebClient kucoinWebClient() {
    return WebClient.builder()
        .baseUrl("https://api.kucoin.com")
        .build();
  }
}
