package com.danielvm.destiny2bot.filter;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.util.CryptoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class SignatureFilterFunction implements
    HandlerFilterFunction<ServerResponse, ServerResponse> {

  private static final String SIGNATURE_HEADER_NAME = "X-Signature-Ed25519";
  private static final String TIMESTAMP_HEADER_NAME = "X-Signature-Timestamp";

  private final DiscordConfiguration discordConfiguration;

  public SignatureFilterFunction(DiscordConfiguration discordConfiguration) {
    this.discordConfiguration = discordConfiguration;
  }

  @Override
  public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
    return DataBufferUtils.join(request.body(BodyExtractors.toDataBuffers()))
        .map(dataBuffer -> {
          byte[] bytes = new byte[dataBuffer.readableByteCount()];
          dataBuffer.read(bytes);
          DataBufferUtils.release(dataBuffer);
          return bytes;
        })
        .flatMap(bytes -> {
          String timestamp = request.headers().header(TIMESTAMP_HEADER_NAME).get(0);
          String signature = request.headers().header(SIGNATURE_HEADER_NAME).get(0);

          Assert.notNull(signature, "Signature header is null");
          Assert.notNull(timestamp, "Signature timestamp is null");

          String publicKey = discordConfiguration.getBotPublicKey();
          boolean isValid = CryptoUtils.validateSignature(bytes, signature, publicKey, timestamp);
          if (!isValid) {
            log.error(
                "There was a request with invalid signature. Signature: [{}], Timestamp: [{}]",
                signature, timestamp);
            String errorMessage = "The signature passed in was invalid. Timestamp: [%s], Signature [%s]"
                .formatted(timestamp, signature);
            ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
            detail.setDetail(errorMessage);
            return ServerResponse.badRequest().body(BodyInserters.fromValue(detail));
          }

          DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
          ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(
              request.exchange().getRequest()) {
            @Override
            public Flux<DataBuffer> getBody() {
              return Flux.just(factory.wrap(bytes));
            }
          };

          var newExchange = request.exchange().mutate().request(modifiedRequest).build();
          ServerRequest newRequest = ServerRequest.create(newExchange, request.messageReaders());
          return next.handle(newRequest);
        });

  }
}
