package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.InteractionResponseEnum.CHANNEL_MESSAGE_WITH_SOURCE;
import static com.danielvm.destiny2bot.enums.InteractionResponseEnum.PONG;
import static com.danielvm.destiny2bot.enums.InteractionType.APPLICATION_COMMAND;
import static com.danielvm.destiny2bot.enums.InteractionType.PING;
import static com.danielvm.destiny2bot.enums.InteractionType.findByValue;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dto.discord.Embedded;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.ActivityModeEnum;
import com.danielvm.destiny2bot.enums.CommandEnum;
import com.danielvm.destiny2bot.util.MessageUtil;
import com.danielvm.destiny2bot.util.OAuth2Params;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class InteractionService {

  private final DiscordConfiguration discordConfiguration;
  private final WeeklyActivitiesService weeklyActivitiesService;

  public InteractionService(
      DiscordConfiguration discordConfiguration,
      WeeklyActivitiesService weeklyActivitiesService) {
    this.discordConfiguration = discordConfiguration;
    this.weeklyActivitiesService = weeklyActivitiesService;
  }

  /**
   * Handles the incoming interactions
   *
   * @param interaction The received interaction from Discord chat
   * @return {@link InteractionResponse}
   */
  public Mono<InteractionResponse> handleInteraction(Interaction interaction) {
    return createResponse(interaction);
  }

  private Mono<InteractionResponse> createResponse(Interaction interaction) {
    var interactionType = findByValue(interaction.getType());
    if (Objects.equals(interactionType, APPLICATION_COMMAND)) {
      var commandName = CommandEnum.findByName(interaction.getData().getName());
      return switch (commandName) {
        case AUTHORIZE -> Mono.just(InteractionResponse.builder()
            .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
            .data(InteractionResponseData.builder()
                .embeds(List.of(
                    Embedded.builder()
                        .title("Link both accounts here")
                        .description("""
                            Riven can grant you wishes unique to your Bungie account.
                            However, you need to link your Discord and Bungie account for that to happen.
                            This slash comma- I mean, this _wish_, allows her to do that.
                            """)
                        .url(buildRegistrationLink())
                        .build())
                ).build()
            ).build());
        case WEEKLY_RAID -> weeklyActivitiesService.getWeeklyActivity(ActivityModeEnum.RAID)
            .map(wr -> {
              var endDay = MessageUtil.FORMATTER.format(wr.getEndDate().toLocalDate());
              return InteractionResponse.builder()
                  .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
                  .data(InteractionResponseData.builder()
                      .content("""
                          This week's raid is: %s.
                          You have until %s to complete it before the next raid comes along.
                          """.formatted(wr.getName(), endDay))
                      .build())
                  .build();
            });
        case WEEKLY_DUNGEON -> weeklyActivitiesService.getWeeklyActivity(ActivityModeEnum.DUNGEON)
            .map(wd -> {
              var formatter = DateTimeFormatter.ofPattern("EEEE MMMM d");
              var endDay = formatter.format(wd.getEndDate().toLocalDate());
              return InteractionResponse.builder()
                  .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
                  .data(InteractionResponseData.builder()
                      .content("""
                          This week's dungeon is: %s.
                          You have until %s to complete it before the next dungeon in the rotation.
                          """.formatted(wd.getName(), endDay))
                      .build())
                  .build();
            });
      };
    } else if (Objects.equals(interactionType, PING)) {
      return Mono.just(InteractionResponse.builder()
          .type(PONG.getType())
          .build());
    }
    return null;
  }

  private String buildRegistrationLink() {
    return UriComponentsBuilder.fromHttpUrl(discordConfiguration.getAuthorizationUrl())
        .queryParam(OAuth2Params.CLIENT_ID, discordConfiguration.getClientId())
        .queryParam(OAuth2Params.REDIRECT_URI,
            URLEncoder.encode(discordConfiguration.getCallbackUrl(),
                StandardCharsets.UTF_8))
        .queryParam(OAuth2Params.RESPONSE_TYPE, OAuth2Params.CODE)
        .queryParam(OAuth2Params.SCOPE, discordConfiguration.getScopes())
        .build().toString();
  }
}
