package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.client.BungieClientWrapper;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RaidStatsServiceTest {

  @Mock
  BungieClient bungieClient;
  @Mock
  BungieClientWrapper bungieClientWrapper;
  @Mock
  UserDetailsRepository userDetailsRepository;
  @Mock
  PostGameCarnageService postGameCarnageService;

  @InjectMocks
  RaidStatsService sut;


}
