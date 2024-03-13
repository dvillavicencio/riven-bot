package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.dto.destiny.ActivitiesResponse;
import com.danielvm.destiny2bot.dto.destiny.BungieResponse;
import com.danielvm.destiny2bot.dto.destiny.characters.UserCharacter;
import com.danielvm.destiny2bot.dto.destiny.manifest.ManifestResponseFields;
import com.danielvm.destiny2bot.dto.destiny.milestone.MilestoneEntry;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import com.danielvm.destiny2bot.exception.InternalServerException;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class BungieAPIService {

  private static final Integer MAX_NUMBER_OF_ELEMENTS = 250;
  private static final Integer RAID_MODE = 4;

  private final BungieClient defaultBungieClient;

  public BungieAPIService(BungieClient defaultBungieClient) {
    this.defaultBungieClient = defaultBungieClient;
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
        .filter(response ->
            Objects.nonNull(response.getResponse()) &&
            CollectionUtils.isNotEmpty(response.getResponse().getActivities()))
        .map(BungieResponse::getResponse)
        .switchIfEmpty(Mono.just(new ActivitiesResponse()));
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
        .flatMap(response -> {
          if (Objects.isNull(response) || Objects.isNull(response.getResponse())
              || Objects.isNull(response.getResponse().getCharacters())) {
            return Mono.error(new ResourceNotFoundException(
                "No available characters were found for user with membershipId [%s] and membership type [%s]".formatted(
                    membershipId, membershipType)));
          }
          return Mono.just(response.getResponse());
        })
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
  @Cacheable(cacheNames = "manifestEntity", cacheManager = "inMemoryCacheManager")
  public Mono<ManifestResponseFields> getManifestEntity(ManifestEntity entityType, Long hash) {
    return defaultBungieClient.getManifestEntity(entityType.getId(), hash).cache()
        .filter(Objects::nonNull)
        .map(BungieResponse::getResponse);
  }

  /**
   * Retrieve all the public milestones
   *
   * @return Map containing all public milestones
   */
  public Mono<Map<String, MilestoneEntry>> getPublicMilestones() {
    return defaultBungieClient.getPublicMilestones()
        .flatMap(response -> {
          if (Objects.isNull(response) || Objects.isNull(response.getResponse())) {
            return Mono.error(new InternalServerException(
                "No available milestone data was available for processing",
                HttpStatus.INTERNAL_SERVER_ERROR));
          }
          return Mono.just(response.getResponse());
        });
  }
}
