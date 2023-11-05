package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.CharactersResponse;
import com.danielvm.destiny2bot.service.CharacterInfoService;
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

    public CharacterInfoController(CharacterInfoService characterInfoService) {
        this.characterInfoService = characterInfoService;
    }

    /**
     * Get information for all characters for the currently logged-in user
     *
     * @return The details for the character
     */
    @GetMapping(value = "/characters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mono<CharactersResponse>> getCharacterInfoForCurrentUser(
            Authentication authentication) throws Exception {
        log.info("GET request received to get all characters");
        var response = characterInfoService.getCharacterInfoForCurrentUser(authentication);
        log.info("GET request completed to get all characters");
        return ResponseEntity.ok(response);
    }
}
