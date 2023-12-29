package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.dto.DestinyCharacter;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.enums.DestinyClass;
import com.danielvm.destiny2bot.enums.DestinyRace;
import com.danielvm.destiny2bot.repository.UserDetailsRepositoryRx;
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
  private final UserDetailsRepositoryRx userDetailsRepositoryRx;
  private final BungieMembershipService bungieMembershipService;

  public DestinyCharacterService(
      BungieClient bungieClient,
      UserDetailsRepositoryRx userDetailsRepositoryRx,
      BungieMembershipService bungieMembershipService) {
    this.bungieClient = bungieClient;
    this.userDetailsRepositoryRx = userDetailsRepositoryRx;
    this.bungieMembershipService = bungieMembershipService;
  }

  /**
   * Retrieves all the characters for a Destiny user
   *
   * @param interaction The Discord interaction
   * @return {@link UserCharacters}
   */
  public Flux<DestinyCharacter> getCharactersForUser(Interaction interaction) {
    return Mono.just(interaction.getMember().getUser().getId())
        .filterWhen(userDetailsRepositoryRx::existsByDiscordId)
        .flatMap(userDetailsRepositoryRx::getUserDetailsByDiscordId)
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
          String characterClass = DestinyClass.findByCode(entry.getValue().getClassType()).getName();
          String characterRace = DestinyRace.findByCode(entry.getValue().getRaceType()).getName();
          Integer lightLevel = entry.getValue().getLight();
          return new DestinyCharacter(characterId, characterClass, lightLevel, characterRace);
        });
  }
}
