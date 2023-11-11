package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.character.info.Characters;
import com.danielvm.destiny2bot.dto.destiny.character.vaultitems.ProfileInventory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

/**
 * This client is responsible for making calls to the Destiny2 Profile endpoint
 */
public interface BungieProfileClient {

    /**
     * Retrieves the character details of the user
     *
     * @param bearerToken         The user's bearer token
     * @param membershipType      The membershipType of the user
     * @param destinyMembershipId The membershipId of the user
     * @return {@link GenericResponse} of {@link Characters}
     */
    @GetExchange("/Destiny2/{membershipType}/Profile/{destinyMembershipId}/?components=200")
    ResponseEntity<GenericResponse<Characters>> getCharacterDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken,
            @PathVariable Integer membershipType,
            @PathVariable String destinyMembershipId);


    /**
     * Retrieves the vault items for a user
     *
     * @param bearerToken         The user's bearer token
     * @param membershipType      The membershipType of the user
     * @param destinyMembershipId The membershipId of the user
     * @return {@link GenericResponse} of {@link ProfileInventory}
     */
    @GetExchange("/Destiny2/{membershipType}/Profile/{destinyMembershipId}/?components=102")
    ResponseEntity<GenericResponse<ProfileInventory>> getCharacterVaultItems(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken,
            @PathVariable Integer membershipType,
            @PathVariable String destinyMembershipId);

    @GetExchange("/Destiny2/{membershipType}/Profile/{destinyMembershipId}/?components=102")
    Mono<GenericResponse<ProfileInventory>> getCharacterVaultItemsRx(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken,
            @PathVariable Integer membershipType,
            @PathVariable String destinyMembershipId);
}
