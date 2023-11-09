package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;

public interface BungieManifestClient {

    @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
    ResponseEntity<GenericResponse<ResponseFields>> getManifestEntity(
            @PathVariable(value = "entityType") String entityType,
            @PathVariable(value = "hashIdentifier") String hashIdentifier);

}
