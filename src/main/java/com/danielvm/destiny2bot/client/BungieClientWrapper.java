package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.BungieResponse;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * This Bungie Client wrapper class is responsible for caching manifest entities
 */
@Component
public class BungieClientWrapper {

  private final BungieClient defaultBungieClient;

  public BungieClientWrapper(BungieClient defaultBungieClient) {
    this.defaultBungieClient = defaultBungieClient;
  }

  /**
   * Wraps the client call to the Manifest with a Cacheable method
   *
   * @param entityType     The entity type (see {@link ManifestEntity})
   * @param hashIdentifier The hash identifier
   * @return {@link BungieResponse} of {@link ResponseFields}
   */
  @Cacheable(cacheNames = "entity", cacheManager = "inMemoryCacheManager")
  public Mono<BungieResponse<ResponseFields>> getManifestEntity(
      ManifestEntity entityType, Long hashIdentifier) {
    return defaultBungieClient.getManifestEntity(entityType.getId(), hashIdentifier).cache();
  }

}
