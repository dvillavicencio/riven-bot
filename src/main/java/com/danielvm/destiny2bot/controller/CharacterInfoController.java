package com.danielvm.destiny2bot.controller;

import com.danielvm.destiny2bot.dto.CharacterDetailsResponse;
import com.danielvm.destiny2bot.dto.destiny.profile.CharacterInfoResponse;
import com.danielvm.destiny2bot.service.CharacterInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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
     * @return {@link CharacterInfoResponse}
     */
    @GetMapping(value = "/characters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CharacterDetailsResponse> getCharacterInfoForCurrentUser() throws Exception {
        log.info("GET request received to get all characters");
        var response = characterInfoService.getCharacterInfoForCurrentUser();
        log.info("GET request completed to get all characters");
        return ResponseEntity.ok(response);
    }
}
