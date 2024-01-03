package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.dao.UserDetailsReactiveDao;
import com.danielvm.destiny2bot.dto.DestinyCharacter;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.enums.DestinyClass;
import com.danielvm.destiny2bot.enums.DestinyRace;
import com.danielvm.destiny2bot.util.MembershipUtil;
import com.danielvm.destiny2bot.util.OAuth2Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DestinyCharacterService {

  private final BungieClient bungieClient;
  private final UserDetailsReactiveDao userDetailsReactiveDao;
  private final BungieMembershipService bungieMembershipService;

  public DestinyCharacterService(
      BungieClient bungieClient,
      UserDetailsReactiveDao userDetailsReactiveDao,
      BungieMembershipService bungieMembershipService) {
    this.bungieClient = bungieClient;
    this.userDetailsReactiveDao = userDetailsReactiveDao;
    this.bungieMembershipService = bungieMembershipService;
  }

  /**
   * Retrieves all the characters for a Destiny user
   *
   * @param interaction The Discord interaction
   * @return Flux of {@link DestinyCharacter}s
   */
  public Flux<DestinyCharacter> getCharactersForUser(Interaction interaction) {
    return Mono.just(interaction.getMember().getUser().getId())
        .filterWhen(userDetailsReactiveDao::existsByDiscordId)
        .flatMap(userDetailsReactiveDao::getByDiscordId)
        .flatMap(userDetails -> {
          String bearerToken = OAuth2Util.formatBearerToken(userDetails.getAccessToken());
          return bungieMembershipService.getUserMembershipInformation(bearerToken);
        })
        .flatMap(membershipResponse -> {
          String membershipId = MembershipUtil.extractMembershipId(membershipResponse);
          Integer membershipType = MembershipUtil.extractMembershipType(membershipResponse);
          return bungieClient.getUserCharacters(membershipType, membershipId);
        })
        .flatMapIterable(characters ->
            characters.getResponse().getCharacters().getData().getCharacterMap().entrySet())
        .map(entry -> {
          String characterId = entry.getKey();
          String characterClass = DestinyClass.findByCode(entry.getValue().getClassType())
              .getName();
          String characterRace = DestinyRace.findByCode(entry.getValue().getRaceType()).getName();
          Integer lightLevel = entry.getValue().getLight();
          return new DestinyCharacter(characterId, characterClass, lightLevel, characterRace);
        });
  }
}
