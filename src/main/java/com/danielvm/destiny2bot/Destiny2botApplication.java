package com.danielvm.destiny2bot;

import com.danielvm.destiny2bot.filter.SignatureFilterFunction;
import com.danielvm.destiny2bot.handler.InteractionHandler;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Slf4j
@EnableCaching
@SpringBootApplication
@EnableAspectJAutoProxy
public class Destiny2botApplication {

  public static void main(String[] args) {
    SpringApplication.run(Destiny2botApplication.class, args);
  }

  @Bean
  RouterFunction<ServerResponse> interactionRouterFunction(
      InteractionHandler interactionHandler,
      SignatureFilterFunction signatureFilterFunction) {
    return RouterFunctions.route()
        .POST("/interactions", interactionHandler::handle)
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

  @Bean
  CacheManager inMemoryCacheManager() {
    return new ConcurrentMapCacheManager();
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
