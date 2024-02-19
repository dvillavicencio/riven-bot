package com.danielvm.destiny2bot.config;

import com.danielvm.destiny2bot.client.BungieClient;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Data
@Configuration
@ConfigurationProperties(prefix = "bungie.api")
public class BungieConfiguration {

  /**
   * The name of the Bungie API key header
   */
  private static final String API_KEY_HEADER_NAME = "x-api-key";

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
   * Base url for Bungie Requests
   */
  private String baseUrl;

  /**
   * Base URL for stats endpoint
   */
  private String statsBaseUrl;

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

  /**
   * Default bungie client used to make general API calls to Bungie.net
   *
   * @param builder The default WebClient.Builder defined in the main application
   * @return {@link BungieClient}
   */
  @Bean("defaultBungieClient")
  public BungieClient bungieCharacterClient(WebClient.Builder builder) {
    var webClient = builder
        .baseUrl(this.baseUrl)
        .defaultHeader(API_KEY_HEADER_NAME, this.key)
        .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs()
            .maxInMemorySize(1024 * 1024))
        .build();
    return HttpServiceProxyFactory.builder()
        .exchangeAdapter(WebClientAdapter.create(webClient))
        .build()
        .createClient(BungieClient.class);
  }

  /**
   * Bungie client used to make API calls to the stats.bungie.net domain
   *
   * @param builder The default WebClient.Builder defined in the main application
   * @return {@link BungieClient}
   */
  @Bean(name = "pgcrBungieClient")
  public BungieClient pgcrBungieClient(WebClient.Builder builder) {
    var webClient = builder
        .baseUrl(this.statsBaseUrl)
        .defaultHeader(API_KEY_HEADER_NAME, this.key)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs()
            .maxInMemorySize(1024 * 1024 * 5))
        .build();
    return HttpServiceProxyFactory.builder()
        .exchangeAdapter(WebClientAdapter.create(webClient))
        .build()
        .createClient(BungieClient.class);
  }
}
