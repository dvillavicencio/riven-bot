package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.dto.destiny.BungieResponse;
import com.danielvm.destiny2bot.dto.destiny.PostGameCarnageReport;
import com.danielvm.destiny2bot.entity.PGCRDetails;
import com.danielvm.destiny2bot.mapper.PGCRMapper;
import com.danielvm.destiny2bot.repository.PGCRRepository;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.time.Duration;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PostGameCarnageService {

  private static final RateLimiter PGCR_RATE_LIMITER = RateLimiter.of("pgcr-rate-limiter",
      RateLimiterConfig.custom()
          .limitForPeriod(25)
          .limitRefreshPeriod(Duration.ofSeconds(1))
          .timeoutDuration(Duration.ofSeconds(30))
          .build());

  private final BungieClient bungieClient;
  private final PGCRMapper pgcrMapper;
  private final PGCRRepository pgcrRepository;

  public PostGameCarnageService(
      BungieClient pgcrBungieClient,
      PGCRMapper pgcrMapper,
      PGCRRepository pgcrRepository) {
    this.bungieClient = pgcrBungieClient;
    this.pgcrMapper = pgcrMapper;
    this.pgcrRepository = pgcrRepository;
  }

  /**
   * Return a Post Game Carnage Report, this method checks to see if the Post Game Carnage Report
   * requested exists in the database before it calls Bungie's API to retrieve it
   *
   * @param activityInstanceId the activity instanceId
   * @return {@link PGCRDetails}
   */
  public Mono<PGCRDetails> retrievePGCR(Long activityInstanceId) {
    PostGameCarnageReport errorFallbackResponse = new PostGameCarnageReport(null, false,
        Collections.emptyList());

    Mono<PGCRDetails> retrievePGCR = bungieClient.getPostGameCarnageReport(activityInstanceId)
        .transformDeferred(RateLimiterOperator.of(PGCR_RATE_LIMITER))
        .onErrorResume(WebClientException.class, err -> {
          log.warn("Response too big to parse, ignoring and falling back to default value", err);
          return Mono.just(new BungieResponse<>(errorFallbackResponse));
        })
        .map(BungieResponse::getResponse)
        .flatMap(
            response -> pgcrRepository.save(pgcrMapper.dtoToEntity(response, activityInstanceId)));

    Mono<PGCRDetails> databasePGCR = pgcrRepository.findById(activityInstanceId);

    return pgcrRepository.existsById(activityInstanceId)
        .flatMap(existsById -> existsById ? databasePGCR : retrievePGCR);
  }

}
