package com.danielvm.destiny2bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

@Data
@Configuration
@ConfigurationProperties(prefix = "bungie.api")
public class BungieApiConfig {

    /**
     * Url for getting membership data for current user
     */
    private String currentUserMembershipUrl;

    /**
     * Url for getting profile data based on membershipId and membershipType
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

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
