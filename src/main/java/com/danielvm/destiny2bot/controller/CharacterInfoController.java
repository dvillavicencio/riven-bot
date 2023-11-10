package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.CharacterVault;
import com.danielvm.destiny2bot.dto.CharacterWeaponsResponse;
import com.danielvm.destiny2bot.dto.CharactersResponse;
import com.danielvm.destiny2bot.service.CharacterInfoService;
import com.danielvm.destiny2bot.service.CharacterWeaponsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@ResponseBody
public class CharacterInfoController {

    private final CharacterInfoService characterInfoService;
    private final CharacterWeaponsService characterWeaponsService;

    public CharacterInfoController(
            CharacterInfoService characterInfoService,
            CharacterWeaponsService characterWeaponsService) {
        this.characterInfoService = characterInfoService;
        this.characterWeaponsService = characterWeaponsService;
    }

    /**
     * Get information for all characters for the currently logged-in user
     *
     * @return The details for the character
     */
    @GetMapping(value = "/characters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CharactersResponse> getCharacterInfoForCurrentUser(
            Authentication authentication) throws Exception {
        log.info("GET request received to get all characters");
        var response = characterInfoService.getCharacterInfoForCurrentUser(authentication);
        log.info("GET request completed to get all characters");
        return ResponseEntity.ok(response);
    }

    /**
     * Get information for all characters for the currently logged-in user
     *
     * @return The details for the character
     */
    @GetMapping(value = "/vault/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CharacterVault> getCharacterVaultItems(
            Authentication authentication) throws Exception {
        log.info("GET request received to get vault items");
        var response = characterWeaponsService.getVaultWeapons(authentication);
        log.info("GET request completed to get vault items");
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/vault/rxItems", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<CharacterVault> getCharacterVaultItemsRx(
            Authentication authentication) throws Exception {
        return characterWeaponsService.getVaultWeaponsRx(authentication);
    }
}
