package com.danielvm.destiny2bot.config;

import com.danielvm.destiny2bot.enums.EntityTypeEnum;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "bungie.api")
public class BungieApiConfig {

    /**
     * Map of manifest definitions to pre-defined classes
     */
    private Map<EntityTypeEnum, String> manifestEntityToClass;

    /**
     * Returns the appropriate class for
     *
     * @param manifestEntity the name of the entity definition
     * @return {@link Class} of the appropriate type defined by the manifestEntityToClass {@link Map}
     */
    public Class<?> getClassForManifestEntity(EntityTypeEnum manifestEntity) {
        try {
            return ClassLoader.getPlatformClassLoader()
                    .loadClass(this.manifestEntityToClass.get(manifestEntity));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Class could not be found for manifest entity [%s]"
                    .formatted(manifestEntity), e);
        }
    }

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

    @Bean(name = "manifestWebClient")
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(this.manifestEntityDefinitionUrl)
                .build();
    }
}
