package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.manifest.ManifestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface BungieManifestClient {

    @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
    ResponseEntity<ManifestEntity> getManifestEntity(
            @PathVariable(value = "entityType") String entityType,
            @PathVariable(value = "hashIdentifier") String hashIdentifier);

}
