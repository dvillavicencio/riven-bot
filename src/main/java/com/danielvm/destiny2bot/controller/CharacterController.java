package com.danielvm.destiny2bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CharacterController {

    @GetMapping("/user")
    public ResponseEntity<UserDetails> getUserDetails() {

    }

}
