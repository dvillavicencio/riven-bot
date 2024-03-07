package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.discord.DiscordUserResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PatchExchange;
import reactor.core.publisher.Mono;

/**
 * This client is responsible for making calls to Discord's API
 */
public interface DiscordClient {

  /**
   * Gets the current Discord user details
   *
   * @param bearerToken The bearer token of the Discord user
   * @return {@link DiscordUserResponse}
   */
  @GetExchange("/users/@me")
  Mono<DiscordUserResponse> getUser(
      @RequestHeader(HttpHeaders.AUTHORIZATION) String bearerToken);

  /**
   * Edit an interaction that was sent already
   *
   * @param applicationId    The id of the application
   * @param interactionToken The interaction token of the original interaction to edit
   * @param data             The data to send
   */
  @PatchExchange(value = "/webhooks/{applicationId}/{interactionToken}/messages/@original", contentType = MediaType.APPLICATION_JSON_VALUE)
  Mono<Void> editOriginalInteraction(
      @PathVariable Long applicationId,
      @PathVariable String interactionToken,
      @RequestBody InteractionResponseData data);
}
