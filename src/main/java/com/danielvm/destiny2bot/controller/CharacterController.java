package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.destinydomain.membership.DestinyMembershipResponse;
import com.danielvm.destiny2bot.dto.destinydomain.profile.DestinyProfileResponse;
import com.danielvm.destiny2bot.service.DestinyCharacterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CharacterController {

    private final DestinyCharacterService destinyCharacterService;

    public CharacterController(DestinyCharacterService destinyCharacterService) {
        this.destinyCharacterService = destinyCharacterService;
    }

    @GetMapping("/character/items")
    public ResponseEntity<DestinyProfileResponse> getUserDetails() throws Exception {
        log.info("GET request received to retrieve all items for characters for the currently logged in user");
        var response = destinyCharacterService.getAllItems();
        log.info("GET request completed to retrieve all items for characters for the currently logged in user");
        return ResponseEntity.ok(response);
    }

}
