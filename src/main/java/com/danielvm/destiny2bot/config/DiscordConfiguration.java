package com.danielvm.destiny2bot.config;

import com.danielvm.destiny2bot.client.DiscordClient;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Data
@Configuration
@ConfigurationProperties(prefix = "discord.api")
public class DiscordConfiguration {

  /**
   * Base Url for Discord API calls
   */
  private String baseUrl;

  /**
   * The token for the Discord Bot
   */
  private String botToken;

  /**
   * The Bot's public key for verifying request signatures
   */
  private String botPublicKey;

  /**
   * The clientId for OAuth2 authentication
   */
  private String clientId;

  /**
   * The clientSecret for OAuth2 authentication
   */
  private String clientSecret;

  /**
   * The callback URL for OAuth2 authentication
   */
  private String callbackUrl;

  /**
   * The authorization url for OAuth2 authentication
   */
  private String authorizationUrl;

  /**
   * Token URL to retrieve access token for OAuth2
   */
  private String tokenUrl;

  /**
   * List of scopes for Discord OAuth2
   */
  private List<String> scopes;

  @Bean
  public DiscordClient discordClient() {
    var webClient = WebClient.builder()
        .baseUrl(this.baseUrl)
        .defaultStatusHandler(code -> code.is4xxClientError() || code.is5xxServerError(),
            clientResponse -> clientResponse.createException()
                .map(ex -> new Exception(ex.getResponseBodyAsString(), ex.getCause())))
        .build();
    return HttpServiceProxyFactory.builder()
        .exchangeAdapter(WebClientAdapter.create(webClient))
        .build().createClient(DiscordClient.class);
  }
}
