package com.danielvm.destiny2bot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "bungie")
public class BungieConfig {

    /**
     * Url for getting membership data for current user
     */
    private String membershipDataForCurrentUserUrl;
}
