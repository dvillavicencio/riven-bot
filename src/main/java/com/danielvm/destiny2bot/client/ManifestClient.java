package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.config.BungieApiConfig;
import com.danielvm.destiny2bot.dto.destiny.manifest.ManifestDto;
import com.danielvm.destiny2bot.enums.EntityTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class ManifestClient extends BungieClient {

    private static final String API_HEADER_NAME = "x-api-key";

    private final WebClient webClient;
    private final BungieApiConfig bungieApiConfig;

    public ManifestClient(
            @Qualifier("manifestWebClient") WebClient webClient,
            BungieApiConfig bungieApiConfig) {
        this.webClient = webClient;
        this.bungieApiConfig = bungieApiConfig;
    }

    /**
     * Get the stat definition for a given stash hash identifier
     *
     * @param statHash The stat hash
     * @return {@link ManifestDto}
     */
    public ManifestDto getStatDefinition(String statHash) {
        return webClient.get()
                .uri(UriComponentsBuilder
                        .fromHttpUrl(bungieApiConfig.getManifestEntityDefinitionUrl())
                        .build(EntityTypeEnum.STAT_DEFINITION.getIdentifier(), statHash))
                .header(API_HEADER_NAME, bungieApiConfig.getKey())
                .exchangeToMono(c -> c.bodyToMono(ManifestDto.class))
                .block();
    }

    /**
     * Get Destiny 2 Class definition
     *
     * @param classHash the class hash to search for
     * @return {@link ManifestDto}
     */
    public ManifestDto getClassDefinition(Long classHash) {
        return webClient.get()
                .uri(UriComponentsBuilder
                        .fromHttpUrl(bungieApiConfig.getManifestEntityDefinitionUrl())
                        .build(EntityTypeEnum.CLASS_DEFINITION.getIdentifier(), classHash))
                .header(API_HEADER_NAME, bungieApiConfig.getKey())
                .exchangeToMono(c -> c.bodyToMono(ManifestDto.class))
                .block();
    }
}
