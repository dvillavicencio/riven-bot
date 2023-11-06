package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.character.info.CharacterDetails;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

public interface BungieCharacterClient {

    @GetExchange("/Destiny2/{membershipType}/Profile/{destinyMembershipId}/?components=200")
    ResponseEntity<CharacterDetails> getCharacterDetails(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken,
            @PathVariable(name = "destinyMembershipId") String membershipId,
            @PathVariable(name = "membershipType") Integer membershipType);
}
