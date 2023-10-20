package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.config.BungieConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class BungieClient {

    private final RestTemplate restTemplate;
    private final BungieConfig bungieConfig;

    public BungieClient(RestTemplateBuilder restTemplateBuilder, BungieConfig bungieConfig) {
        this.restTemplate = restTemplateBuilder.build();
        this.bungieConfig = bungieConfig;
    }



}
