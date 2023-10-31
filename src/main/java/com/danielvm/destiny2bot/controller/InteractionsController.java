package com.danielvm.destiny2bot.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class InteractionsController {

    @PostMapping(value = "/interactions")
    public ResponseEntity<> getInteractions() {
        return null;
    }
}
