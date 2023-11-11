package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class BungieManifestClientWrapper {

    private final BungieManifestClient bungieManifestClient;

    @Cacheable(cacheNames = "entity", cacheManager = "inMemoryCacheManager")
    public Mono<GenericResponse<ResponseFields>> getManifestEntityRx(String entityType, String hashIdentifier) {
        log.info("Getting manifest {} {}", entityType, hashIdentifier);
        return bungieManifestClient.getManifestEntityRx(entityType, hashIdentifier).cache();
    }

}
