package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.destiny.characters.CharacterWeaponsResponse;
import com.danielvm.destiny2bot.service.WeaponsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CharacterWeaponsController {

    private final WeaponsService destinyCharacterService;

    public CharacterWeaponsController(WeaponsService destinyCharacterService) {
        this.destinyCharacterService = destinyCharacterService;
    }

    /**
     * Get all the weapons per inventory for all characters
     *
     * @return {@link CharacterWeaponsResponse}
     * @throws Exception exception
     */
    @GetMapping("/characters/weapons")
    public ResponseEntity<CharacterWeaponsResponse> getUserDetails() throws Exception {
        log.info("GET request received to retrieve all items for characters for the current Bungie user");
        var response = destinyCharacterService.getAllWeapons();
        log.info("GET request completed to retrieve all items for characters for the current Bungie user");
        return ResponseEntity.ok(response);
    }

}
