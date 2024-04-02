package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.destiny.BungieResponse;
import com.danielvm.destiny2bot.dto.destiny.PostGameCarnageReport;
import com.danielvm.destiny2bot.entity.PGCRDetails;
import com.danielvm.destiny2bot.mapper.PGCRMapper;
import com.danielvm.destiny2bot.repository.PGCRRepository;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.netty.handler.codec.DecoderException;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.ResolvableType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class PostGameCarnageService {

  private static final RateLimiter PGCR_RATE_LIMITER = RateLimiter.of("pgcr-rate-limiter",
      RateLimiterConfig.custom()
          .limitForPeriod(23)
          .limitRefreshPeriod(Duration.ofMillis(1500))
          .timeoutDuration(Duration.ofSeconds(30))
          .build());

  private final WebClient.Builder builder;
  private final PGCRMapper pgcrMapper;
  private final PGCRRepository pgcrRepository;

  public PostGameCarnageService(
      Builder builder, PGCRMapper pgcrMapper,
      PGCRRepository pgcrRepository) {
    this.builder = builder;
    this.pgcrMapper = pgcrMapper;
    this.pgcrRepository = pgcrRepository;
  }

  /**
   * Return a Post Game Carnage Report, this method checks to see if the Post Game Carnage Report
   * requested exists in the database before it calls Bungie's API to retrieve it
   *
   * @param activityInstanceId the activity instanceId
   * @return {@link PGCRDetails}
   * @noinspection unchecked
   */
  public Mono<PGCRDetails> retrievePGCR(Long activityInstanceId) {
    WebClient webClient = builder.build();

    Flux<DataBuffer> dataChunks = webClient.get()
        .uri("/Destiny2/Stats/PostGameCarnageReport/{activityId}/", activityInstanceId)
        .exchangeToFlux(clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers())
            .concatMap(dataBuffer -> {
              AtomicInteger currentSize = new AtomicInteger(0);
              int chunkSize = dataBuffer.readableByteCount();
              if (chunkSize + currentSize.get() > 16_000) {
                return Mono.empty();
              } else {
                currentSize.addAndGet(chunkSize);
                return Mono.just(dataBuffer);
              }
            }))
        .transformDeferred(RateLimiterOperator.of(PGCR_RATE_LIMITER));

    ParameterizedTypeReference<BungieResponse<PostGameCarnageReport>> typeReference =
        new ParameterizedTypeReference<>() {
        };
    var remotePGCR = DataBufferUtils.join(dataChunks)
        .mapNotNull(dataBuffer -> {
          Jackson2JsonDecoder decoder = new Jackson2JsonDecoder();
          if (decoder.canDecode(ResolvableType.forType(typeReference), null)) {
            return (BungieResponse<PostGameCarnageReport>) decoder.decode(dataBuffer,
                ResolvableType.forType(typeReference), null, null);
          } else {
            throw new DecoderException(
                "Unable to decode Post Game Carnage Report to their respective object");
          }
        })
        .map(BungieResponse::getResponse)
        .flatMap(
            response -> pgcrRepository.save(pgcrMapper.dtoToEntity(response, activityInstanceId)))
        .doOnDiscard(DataBuffer.class, DataBufferUtils::release);

    Mono<PGCRDetails> cachedPGCR = pgcrRepository.findById(activityInstanceId);

    return pgcrRepository.existsById(activityInstanceId)
        .flatMap(existsById -> existsById ? cachedPGCR : remotePGCR);
  }

}
