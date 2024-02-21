package com.danielvm.destiny2bot.filter;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.util.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Order(1)
@Slf4j
public class ValidSignatureWebFilter implements WebFilter {

  private static final String SIGNATURE_HEADER_NAME = "X-Signature-Ed25519";
  private static final String TIMESTAMP_HEADER_NAME = "X-Signature-Timestamp";

  private final DiscordConfiguration discordConfiguration;

  public ValidSignatureWebFilter(DiscordConfiguration discordConfiguration) {
    this.discordConfiguration = discordConfiguration;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return chain.filter(exchange);
  }

//  @Override
//  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
//    ServerHttpRequest request = exchange.getRequest();
//    return DataBufferUtils.join(exchange.getRequest().getBody())
//        .map(dataBuffer -> {
//          byte[] bytes = new byte[dataBuffer.readableByteCount()];
//          dataBuffer.read(bytes);
//          DataBufferUtils.release(dataBuffer);
//          return bytes;
//        })
//        .flatMap(bytes -> {
//          Assert.notNull(request.getHeaders().get(SIGNATURE_HEADER_NAME),
//              "Signature header is null");
//          Assert.notNull(request.getHeaders().get(TIMESTAMP_HEADER_NAME),
//              "Signature timestamp is null");
//
//          String signature = request.getHeaders().get(SIGNATURE_HEADER_NAME).getFirst();
//          String timestamp = request.getHeaders().get(TIMESTAMP_HEADER_NAME).getFirst();
//          String publicKey = discordConfiguration.getBotPublicKey();
//          boolean isValid = CryptoUtil.validateSignature(bytes, signature, publicKey, timestamp);
//          if (!isValid) {
//            log.error(
//                "There was a request with invalid signature. Signature: [{}], Timestamp: [{}]",
//                signature, timestamp);
//            String errorMessage = "The signature passed in was invalid. Timestamp: [%s], Signature [%s]"
//                .formatted(timestamp, signature);
//            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
//            DataBuffer buffer = exchange.getResponse().bufferFactory()
//                .wrap(errorMessage.getBytes());
//            return exchange.getResponse().writeWith(Mono.just(buffer));
//          }
//
//          DefaultDataBufferFactory factory = new DefaultDataBufferFactory();
//          ServerHttpRequest modifiedRequest = new ServerHttpRequestDecorator(request) {
//            @Override
//            public Flux<DataBuffer> getBody() {
//              return Flux.just(factory.wrap(bytes));
//            }
//          };
//
//          ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();
//          return chain.filter(modifiedExchange);
//        });
//  }
}
