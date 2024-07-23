package com.deahtstroke.rivenbot.service;

import com.deahtstroke.rivenbot.client.BungieClient;
import com.deahtstroke.rivenbot.dto.destiny.ActivitiesResponse;
import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.SearchResult;
import com.deahtstroke.rivenbot.dto.destiny.UserGlobalSearchBody;
import com.deahtstroke.rivenbot.dto.destiny.characters.UserCharacter;
import com.deahtstroke.rivenbot.dto.destiny.manifest.ManifestResponseFields;
import com.deahtstroke.rivenbot.dto.destiny.milestone.MilestoneEntry;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.enums.ManifestEntity;
import com.deahtstroke.rivenbot.exception.ManifestEntityNotFoundException;
import com.deahtstroke.rivenbot.exception.NoCharactersFoundException;
import com.deahtstroke.rivenbot.exception.ResourceNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BungieAPIService {

  private static final Integer MAX_NUMBER_OF_ELEMENTS = 250;
  private static final String PREFIX_PLAYERS_URL = "/User/Search/GlobalName/{pageNumber}/";
  private static final Integer RAID_MODE = 4;

  private final BungieClient defaultBungieClient;
  private final WebClient webClient;

  public BungieAPIService(
      BungieClient defaultBungieClient,
      WebClient defaultBungieWebClient) {
    this.defaultBungieClient = defaultBungieClient;
    this.webClient = defaultBungieWebClient;
  }

  /**
   * Get the raid activities from the user's activity history for a character
   *
   * @param membershipType The membership type of the bungie.net user
   * @param membershipId   The membershipId of the bungie.net user
   * @param characterId    The characterId of the bungie.net user
   * @param pageNumber     The page number to request
   * @return {@link ActivitiesResponse}
   */
  public Mono<ActivitiesResponse> getRaidActivities(Integer membershipType, String membershipId,
      String characterId, Integer pageNumber) {
    return defaultBungieClient.getActivityHistory(membershipType, membershipId, characterId,
            MAX_NUMBER_OF_ELEMENTS, RAID_MODE, pageNumber)
        .filter(data ->
            Objects.nonNull(data.getResponse()) &&
            CollectionUtils.isNotEmpty(data.getResponse().getActivities()))
        .switchIfEmpty(
            Mono.just(BungieResponse.of(new ActivitiesResponse(Collections.emptyList()))))
        .map(BungieResponse::getResponse);
  }

  /**
   * Return a map of character details for a bungie.net user
   *
   * @param membershipType The membership type of the bungie.net user
   * @param membershipId   The membershipId of the bungie.net user
   * @return A map with the key being the characterIds and the values being character-specific
   * details
   */
  public Mono<Map<String, UserCharacter>> getUserCharacters(Integer membershipType,
      String membershipId) {
    return defaultBungieClient.getUserCharacters(membershipType, membershipId)
        .filter(data -> Objects.equals(data.getErrorCode(), 1))
        .filter(data -> CollectionUtils.isNotEmpty(
            data.getResponse().getCharacters().getData().entrySet()))
        .switchIfEmpty(Mono.error(new NoCharactersFoundException(
            "No characters found for user with ID [%s] and membership type [%s]".formatted(
                membershipId, membershipType),
            InteractionResponseData.builder()
                .content(
                    "It seems that the user you were trying to find does not have any Destiny 2 characters")
                .build()
        )))
        .doOnError(err -> log.error(err.getMessage()))
        .map(BungieResponse::getResponse)
        .map(response -> response.getCharacters().getData());
  }

  /**
   * Retrieves a manifest entity from Bungie's manifest database, checking the cache first before
   * calling Bungie
   *
   * @param entityType The type of the entity
   * @param hash       The hash of the entity
   * @return {@link ManifestResponseFields}
   */
  @Cacheable(cacheNames = "manifestEntity", cacheManager = "redisCacheManager")
  public Mono<ManifestResponseFields> getManifestEntity(ManifestEntity entityType, Long hash) {
    return defaultBungieClient.getManifestEntity(entityType.getId(), hash).cache()
        .filter(me -> Objects.nonNull(me) && Objects.nonNull(me.getResponse()))
        .switchIfEmpty(Mono.error(new ManifestEntityNotFoundException(
            "Manifest entity not found for [%s] and hash [%s]".formatted(entityType, hash),
            HttpStatus.INTERNAL_SERVER_ERROR)))
        .map(BungieResponse::getResponse);
  }

  /**
   * Get public milestones
   *
   * @return Map containing Strings as keys and {@link MilestoneEntry} as values
   */
  public Mono<Map<String, MilestoneEntry>> getPublicMilestones() {
    return defaultBungieClient.getPublicMilestones()
        .flatMap(response -> {
          if (Objects.isNull(response) || Objects.isNull(response.getResponse())) {
            return Mono.error(new ResourceNotFoundException(
                "No available milestone data was available for processing"));
          }
          return Mono.just(response.getResponse());
        });
  }

  /**
   * Get players by their basename prefix. This method is resilient towards 500 status error codes
   * because the API itself is not consistent with the 'hasMore' flag if there's more pages in the
   * API itself
   *
   * @param searchBody the prefix to look for
   * @param page       the number of the page
   * @return {@link SearchResult}
   */
  @Cacheable(cacheNames = "playersPrefixSearch", cacheManager = "redisCacheManager")
  public Mono<BungieResponse<SearchResult>> retrievePlayers(UserGlobalSearchBody searchBody,
      Integer page) {
    return webClient.post().uri(PREFIX_PLAYERS_URL, page)
        .body(BodyInserters.fromValue(searchBody))
        .exchangeToMono(clientResponse -> {
          var status = clientResponse.statusCode();
          if (status.is4xxClientError()) {
            return Mono.error(new ResourceNotFoundException(
                "Something wrong happened while retrieving users, user not found with value [%s]".formatted(
                    searchBody)));
          } else {
            ParameterizedTypeReference<BungieResponse<SearchResult>> typeReference = new ParameterizedTypeReference<>() {
            };
            return clientResponse.bodyToMono(typeReference);
          }
        });
  }
}
