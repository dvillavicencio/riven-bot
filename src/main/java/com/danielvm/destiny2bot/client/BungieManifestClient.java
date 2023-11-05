package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.manifest.ManifestEntity;
import com.danielvm.destiny2bot.enums.EntityTypeEnum;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface BungieManifestClient {

    @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
    Mono<ManifestEntity> getManifestEntity(
            @PathVariable(value = "entityType") EntityTypeEnum entityType,
            @PathVariable(value = "hashIdentifier") String hashIdentifier);

}
