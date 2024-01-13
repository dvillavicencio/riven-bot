package com.danielvm.destiny2bot.factory;

import static com.danielvm.destiny2bot.enums.InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dto.discord.Component;
import com.danielvm.destiny2bot.dto.discord.Embedded;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.util.OAuth2Util;
import java.util.List;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthorizeMessageCreator implements MessageResponse {

  public static final String MESSAGE_TITLE = "**Link Bungie and Discord accounts here**";
  public static final String MESSAGE_DESCRIPTION = """
      Riven can grant you wishes unique to your Destiny 2 characters.
            
      However, in order for her to do that you must authorize her to read a sub-set of your Destiny 2 data beforehand.
      """;
  private static final Integer EPHEMERAL_BYTE = 1000000;
  private final DiscordConfiguration discordConfiguration;

  public AuthorizeMessageCreator(DiscordConfiguration discordConfiguration) {
    this.discordConfiguration = discordConfiguration;
  }

  @Override
  public Mono<InteractionResponse> commandResponse(Interaction interaction) {
    String authUrl = discordConfiguration.getAuthorizationUrl();
    String clientId = discordConfiguration.getClientId();
    String callbackUrl = discordConfiguration.getCallbackUrl();
    String scopes = String.join(",", discordConfiguration.getScopes());

    Embedded accountLinkEmbed = Embedded.builder()
        .title(MESSAGE_TITLE)
        .description(MESSAGE_DESCRIPTION)
        .build();

    return Mono.just(InteractionResponse.builder()
        .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(InteractionResponseData.builder()
            .embeds(List.of(accountLinkEmbed))
            .flags(EPHEMERAL_BYTE)
            .components(
                List.of(Component.builder()
                    .type(1)
                    .components(List.of(
                        Component.builder() // 'Authorize' link button
                            .type(2)
                            .style(5)
                            .url(OAuth2Util.discordAuthorizationUrl(authUrl, clientId,
                                callbackUrl, scopes))
                            .label("Authorize")
                            .build(),
                        Component.builder() // 'Why?' button
                            .customId("why_authorize_button")
                            .label("Why?")
                            .type(2)
                            .style(1)
                            .build())
                    )
                    .build()))
            .build())
        .build());
  }

  @Override
  public Mono<InteractionResponse> autocompleteResponse(Interaction interaction) {
    return null;
  }
}
