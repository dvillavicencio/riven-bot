package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DestinyUserService {

    private final BungieClient bungieClient;

    public DestinyUserService(BungieClient bungieClient) {
        this.bungieClient = bungieClient;
    }


}
