package com.danielvm.destiny2bot.config;

import com.danielvm.destiny2bot.client.BungieClient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "bungie.api")
public class BungieConfiguration {

  private static final String API_KEY_HEADER_NAME = "x-api-key";

  /**
   * Url for getting membership characters for current user
   */
  private String currentUserMembershipUrl;

  /**
   * Url for getting profile characters based on membershipId and membershipType
   */
  private String profileDataUrl;

  /**
   * API key provided by Bungie when registering an application in their portal
   */
  private String key;

  /**
   * Bungie clientId
   */
  private String clientId;

  /**
   * Bungie client secret
   */
  private String clientSecret;

  /**
   * Url for getting manifest definitions of things, based on hashes
   */
  private String manifestEntityDefinitionUrl;

  /**
   * Base url for Bungie Requests
   */
  private String baseUrl;

  /**
   * Url for Bungie Token endpoint
   */
  private String tokenUrl;

  /**
   * Url for OAuth2 authorization flow
   */
  private String authorizationUrl;

  /**
   * Url for callback during OAuth2 authorization
   */
  private String callbackUrl;

  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

  @Bean
  public BungieClient bungieCharacterClient() {
    return createClient();
  }


  private BungieClient createClient() {
    var webClient = WebClient.builder()
        .baseUrl(this.baseUrl)
        .defaultHeader(API_KEY_HEADER_NAME, this.key)
        .defaultStatusHandler(code -> code.is4xxClientError() || code.is5xxServerError(),
            clientResponse -> clientResponse.createException()
                .map(ex -> new Exception(ex.getResponseBodyAsString(), ex.getCause())))
        .build();
    return HttpServiceProxyFactory.builder()
        .exchangeAdapter(WebClientAdapter.create(webClient))
        .build().createClient(BungieClient.class);
  }
}
