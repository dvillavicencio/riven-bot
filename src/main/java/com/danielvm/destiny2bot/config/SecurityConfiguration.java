package com.danielvm.destiny2bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/character/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/characters**").authenticated()
                        .requestMatchers("/**").permitAll())
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(c -> c.userService(this.customOAuth2Service())))
                .oauth2Client(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }

    /**
     * Custom OAuth2UserService that does not go through the user-info endpoint to retrieve scopes, but instead
     * returns the access_token in the attributes map
     *
     * @return {@link OAuth2UserService}
     */
    private OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2Service() {
        return userRequest -> new DefaultOAuth2User(
                AuthorityUtils.NO_AUTHORITIES,
                Map.of(
                        "access_token", userRequest.getAccessToken().getTokenValue(),
                        "membership_id", userRequest.getAdditionalParameters().get("membership_id"),
                        "name", "no name"),
                "name");
    }

}
