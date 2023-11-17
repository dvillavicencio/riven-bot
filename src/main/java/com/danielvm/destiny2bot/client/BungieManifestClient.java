package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface BungieManifestClient {

    /**
     * Ges a manifest entity from the Manifest API
     *
     * @param entityType     The entity type (see {@link com.danielvm.destiny2bot.enums.EntityTypeEnum})
     * @param hashIdentifier The entity hash identifier
     * @return {@link GenericResponse} of {@link ResponseFields}
     */
    @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
    ResponseEntity<GenericResponse<ResponseFields>> getManifestEntity(
            @PathVariable(value = "entityType") String entityType,
            @PathVariable(value = "hashIdentifier") String hashIdentifier);

    /**
     * Ges a manifest entity from the Manifest API asynchronously
     *
     * @param entityType     The entity type (see {@link com.danielvm.destiny2bot.enums.EntityTypeEnum})
     * @param hashIdentifier The entity hash identifier
     * @return {@link GenericResponse} of {@link ResponseFields}
     */
    @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
    Mono<GenericResponse<ResponseFields>> getManifestEntityRx(
            @PathVariable(value = "entityType") String entityType,
            @PathVariable(value = "hashIdentifier") String hashIdentifier);

}
