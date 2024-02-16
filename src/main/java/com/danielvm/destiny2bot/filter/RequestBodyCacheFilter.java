package com.danielvm.destiny2bot.filter;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class RequestBodyCacheFilter implements WebFilter {

  private final DiscordConfiguration discordConfiguration;

  public RequestBodyCacheFilter(DiscordConfiguration discordConfiguration) {
    this.discordConfiguration = discordConfiguration;
  }

  // TODO: Implement a web filter that caches the request body for verifying signature
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) { return null;
  }
}
