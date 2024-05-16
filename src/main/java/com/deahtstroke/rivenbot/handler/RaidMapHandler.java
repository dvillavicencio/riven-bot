package com.deahtstroke.rivenbot.handler;

import com.deahtstroke.rivenbot.dto.discord.Attachment;
import com.deahtstroke.rivenbot.dto.discord.Choice;
import com.deahtstroke.rivenbot.dto.discord.Component;
import com.deahtstroke.rivenbot.dto.discord.Embedded;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedFooter;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedImage;
import com.deahtstroke.rivenbot.dto.discord.Emoji;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.dto.discord.Option;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.enums.Raid;
import com.deahtstroke.rivenbot.enums.RaidEncounter;
import com.deahtstroke.rivenbot.exception.InternalServerException;
import com.deahtstroke.rivenbot.service.RaidInfographicsService;
import com.deahtstroke.rivenbot.util.InteractionUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@Slf4j
@org.springframework.stereotype.Component
public class RaidMapHandler implements ApplicationCommandSource, AutocompleteSource {

  private static final String EMBED_BINDING_URL = "https://www.youtube.com/watch?v=dQw4w9WgXcQ";
  private static final String EMBED_TITLE = "%s at %s";
  private static final String RAID_OPTION_NAME = "raid";
  private static final String EMBED_IMAGE_TYPE = "image";
  private static final String ENCOUNTER_OPTION_NAME = "encounter";
  private static final String ARTIST_CREDIT_FOOTER_FORMAT = "Infographics by %s!";
  private static final String SOCIAL_LINK_LABEL_FORMAT = "%s's %s";

  private final RaidInfographicsService raidInfographicsService;

  public RaidMapHandler(
      RaidInfographicsService raidInfographicsService) {
    this.raidInfographicsService = raidInfographicsService;
  }

  private static InteractionResponse formatInteractionResponse(
      Interaction interaction, List<Attachment> attachments) {
    Assert.notNull(interaction.getData().getOptions(),
        "The options for the interaction cannot be null");
    List<Option> interactionOptions = interaction.getData().getOptions();

    String encounterDirectory = InteractionUtils.retrieveInteractionOption(
        interactionOptions, ENCOUNTER_OPTION_NAME);
    String raidDirectory = InteractionUtils.retrieveInteractionOption(
        interactionOptions, RAID_OPTION_NAME);

    Raid raid = Raid.findRaid(raidDirectory);

    RaidEncounter raidEncounter = RaidEncounter.findEncounter(raid, encounterDirectory);

    List<Embedded> embeds = attachments.stream()
        .map(attachment -> Embedded.builder()
            .title(EMBED_TITLE.formatted(raid.getRaidName(), raidEncounter.getName()))
            .url(EMBED_BINDING_URL)
            .type(EMBED_IMAGE_TYPE)
            .image(EmbeddedImage.builder()
                .url("attachment://" + attachment.getFilename())
                .build())
            .footer(EmbeddedFooter.builder()
                .text(ARTIST_CREDIT_FOOTER_FORMAT.formatted(raid.getArtistName()))
                .build())
            .build())
        .toList();
    InteractionResponseData data = InteractionResponseData.builder()
        .components(List.of(Component.builder()
            .type(1)
            .components(raid.getArtistSocials().stream()
                .map(socialLink -> Component.builder()
                    .type(2)
                    .style(5)
                    .url(socialLink.getSocialLink())
                    .label(SOCIAL_LINK_LABEL_FORMAT.formatted(raid.getArtistName(),
                        socialLink.getSocialPlatform().getPlatformName()))
                    .emoji(new Emoji(socialLink.getSocialPlatform().getEmojiId(),
                        socialLink.getSocialPlatform().getEmojiName(), false))
                    .build())
                .toList())
            .build()))
        .attachments(attachments)
        .embeds(embeds)
        .build();
    return InteractionResponse.builder()
        .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(data)
        .build();
  }

  private static List<Attachment> extractAttachments(Map<Long, Resource> map) {
    return map.entrySet().stream()
        .map(entry -> {
          try {
            return Attachment.builder()
                .id(entry.getKey())
                .filename(entry.getValue().getFilename())
                .size(Math.toIntExact(entry.getValue().contentLength()))
                .build();
          } catch (IOException e) {
            String errorMessage = "Something went wrong while parsing filename for resource [%s]"
                .formatted(entry.getValue());
            throw new InternalServerException(errorMessage, e);
          }
        })
        .toList();
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    return raidInfographicsService.retrieveEncounterImages(interaction)
        .map(RaidMapHandler::extractAttachments)
        .map(attachments -> formatInteractionResponse(interaction, attachments))
        .onErrorResume(IOException.class, err -> {
          String raidName = InteractionUtils.retrieveInteractionOption(interaction.getData()
              .getOptions(), ENCOUNTER_OPTION_NAME);
          String errorMessage = "Something wrong happened while retrieving encounter images for raid [%s]"
              .formatted(raidName);
          log.error(errorMessage, err);
          return Mono.error(new InternalServerException(errorMessage, err));
        });
  }

  @Override
  public Mono<InteractionResponse> autocompleteResponse(Interaction interaction) {
    return Flux.fromIterable(interaction.getData().getOptions())
        .filter(option ->
            Objects.equals(option.getName(), RAID_OPTION_NAME) &&
            Objects.equals(option.getType(), 3)) // 3 is for String data type
        .flatMap(option ->
            Flux.fromIterable(Arrays.asList(Raid.values()))
                .filter(raid -> raid.getRaidDirectory()
                    .equalsIgnoreCase(String.valueOf(option.getValue())))
                .next())
        .flatMap(raid ->
            Mono.just(raid)
                .flatMapMany(RaidEncounter::getRaidEncounters)
                .map(encounter ->
                    new Choice(encounter.getName(), encounter.getDirectory()))
        )
        .collectList()
        .map(encounters ->
            InteractionResponse.builder()
                .type(InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType())
                .data(InteractionResponseData.builder()
                    .choices(encounters)
                    .build())
                .build());
  }
}
