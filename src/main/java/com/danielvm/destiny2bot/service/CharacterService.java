package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CharacterService {

  private final BungieClient bungieClient;

  public CharacterService(BungieClient bungieClient) {
    this.bungieClient = bungieClient;
  }


}
