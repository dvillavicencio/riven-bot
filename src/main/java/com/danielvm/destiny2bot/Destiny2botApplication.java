package com.danielvm.destiny2bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Destiny2botApplication {

    public static void main(String[] args) {
        SpringApplication.run(Destiny2botApplication.class, args);
    }

//    @Bean
//    public RestTemplateBuilder restTemplate(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
//        return new RestTemplateBuilder();
//    }
}
