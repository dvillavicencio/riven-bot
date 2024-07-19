package com.deahtstroke.rivenbot;

import com.deahtstroke.rivenbot.filter.SignatureFilterFunction;
import com.deahtstroke.rivenbot.handler.InteractionHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Slf4j
@SpringBootApplication
public class RivenBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(RivenBotApplication.class, args);
  }

  @Bean
  RouterFunction<ServerResponse> interactionRouterFunction(
      InteractionHandler interactionHandler,
      SignatureFilterFunction signatureFilterFunction) {
    return RouterFunctions.route()
        .POST("/interactions", interactionHandler::resolveRequest)
        .filter(signatureFilterFunction)
        .build();
  }

  /**
   * Default object mapper that does not fail if properties are not known
   *
   * @return {@link ObjectMapper}
   */
  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }

  /**
   * Prepares a WebClient.Builder bean that has standard status handlers in case of 4xx client
   * request errors and 5xx server errors
   *
   * @return {@link WebClient.Builder}
   */
  @Bean
  public WebClient.Builder webClient() {
    return WebClient.builder();
  }

  @Bean
  PathMatchingResourcePatternResolver resourcePatternResolver() {
    return new PathMatchingResourcePatternResolver();
  }
}
