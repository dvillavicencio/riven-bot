package com.danielvm.destiny2bot.config;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.client.BungieMembershipClient;
import com.danielvm.destiny2bot.client.BungieProfileClient;
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
     * Url for getting manifest definitions of things, based on hashes
     */
    private String manifestEntityDefinitionUrl;

    /**
     * Base url for Bungie Requests
     */
    private String baseUrl;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public BungieMembershipClient bungieMembershipClient() {
        return createClient(BungieMembershipClient.class);
    }

    @Bean
    public BungieProfileClient bungieCharacterClient() {
        return createClient(BungieProfileClient.class);
    }

    @Bean
    public BungieManifestClient bungieManifestClient() {
        return createClient(BungieManifestClient.class);
    }

    private <T> T createClient(Class<T> clientType) {
        var webClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader(API_KEY_HEADER_NAME, this.key)
                .defaultStatusHandler(code -> code.is4xxClientError() || code.is5xxServerError(),
                        clientResponse -> clientResponse.createException()
                                .map(ex -> new Exception(ex.getResponseBodyAsString(), ex.getCause())))
                .build();
        return HttpServiceProxyFactory.builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build().createClient(clientType);
    }
}
