package com.deahtstroke.rivenbot.service;

import com.deahtstroke.rivenbot.config.BungieConfiguration;
import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.PostGameCarnageReport;
import com.deahtstroke.rivenbot.entity.PGCRDetails;
import com.deahtstroke.rivenbot.exception.PGCRSizeLimitException;
import com.deahtstroke.rivenbot.mapper.PGCRMapper;
import com.deahtstroke.rivenbot.repository.PGCRRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

// Short for PostGameCarnageReportService
@Service
@Slf4j
public class PGCRService {

  private static final String PGCR_ENDPOINT_URL = "/Destiny2/Stats/PostGameCarnageReport/{activityId}/";
  private static final Integer PGCR_SIZE_LIMIT_BYTES = 32_000;

  private final WebClient webClient;
  private final PGCRMapper pgcrMapper;
  private final PGCRRepository pgcrRepository;
  private final ObjectMapper objectMapper;

  public PGCRService(
      WebClient pgcrWebClient,
      PGCRMapper pgcrMapper,
      PGCRRepository pgcrRepository,
      ObjectMapper objectMapper) {
    this.pgcrMapper = pgcrMapper;
    this.pgcrRepository = pgcrRepository;
    this.objectMapper = objectMapper;
    this.webClient = pgcrWebClient;
  }

  /**
   * Return a Post Game Carnage Report, this method checks to see if the Post Game Carnage Report
   * requested exists in the database before it calls Bungie's API to retrieve it
   *
   * @param activityInstanceId the activity instanceId
   * @return {@link PGCRDetails}
   */
  public Mono<PGCRDetails> retrievePGCR(Long activityInstanceId) {
    AtomicInteger currentSize = new AtomicInteger(0);
    return pgcrRepository.existsById(activityInstanceId)
        .flatMap(existsById -> {
          if (Boolean.TRUE.equals(existsById)) {
            return pgcrRepository.findById(activityInstanceId);
          } else {
            return webClient.get()
                .uri(PGCR_ENDPOINT_URL, activityInstanceId)
                .exchangeToFlux(
                    clientResponse -> clientResponse.body(BodyExtractors.toDataBuffers()))
                .concatMap(dataBuffer -> {
                  int chunkSize = dataBuffer.readableByteCount();
                  if (dataBuffer.readableByteCount() + currentSize.get() > PGCR_SIZE_LIMIT_BYTES) {
                    return Mono.error(new PGCRSizeLimitException(
                        "PGCR with Id [%s] exceeded the size limit of 16 KBs".formatted(
                            activityInstanceId)));
                  } else {
                    currentSize.addAndGet(chunkSize);
                    return Mono.just(dataBuffer);
                  }
                })
                .transformDeferred(RateLimiterOperator.of(BungieConfiguration.PGCR_RATE_LIMITER))
                .collectList()
                .flatMap(buffers -> DataBufferUtils.join(Flux.fromIterable(buffers)))
                .flatMap(buffer -> {
                  byte[] bytes = new byte[buffer.readableByteCount()];
                  buffer.read(bytes);
                  DataBufferUtils.release(buffer);
                  var type = new TypeReference<BungieResponse<PostGameCarnageReport>>() {
                  };
                  return Mono.defer(
                          () -> Mono.fromCallable(() -> objectMapper.readValue(bytes, type)))
                      .subscribeOn(Schedulers.boundedElastic());
                })
                .onErrorResume(PGCRSizeLimitException.class, ex -> Mono.just(
                    BungieResponse.of(PostGameCarnageReport.EMPTY_RESPONSE)))
                .flatMap(response -> pgcrRepository.save(
                    pgcrMapper.dtoToEntity(response.getResponse(), activityInstanceId)))
                .doOnDiscard(DataBuffer.class, DataBufferUtils::release);
          }
        });
  }
}
