package com.deahtstroke.rivenbot.service;

import com.deahtstroke.rivenbot.client.DiscordClient;
import com.deahtstroke.rivenbot.config.DiscordConfiguration;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class DiscordAPIService {

  private final DiscordConfiguration discordConfiguration;
  private final DiscordClient discordClient;

  public DiscordAPIService(
      DiscordConfiguration discordConfiguration,
      DiscordClient discordClient) {
    this.discordConfiguration = discordConfiguration;
    this.discordClient = discordClient;
  }

  /**
   * Edit the original interaction based on an Interaction token
   *
   * @param interactionToken the interaction token to update the original interaction sent
   * @param data             the data to send
   */
  public Mono<Void> editOriginalInteraction(String interactionToken, InteractionResponseData data) {
    Long applicationId = discordConfiguration.getApplicationId();
    return discordClient.editOriginalInteraction(applicationId, interactionToken, data)
        .doOnSuccess(s -> log.info("Updating message with interaction token [{}] with data [{}]",
            interactionToken, data));
  }

}
