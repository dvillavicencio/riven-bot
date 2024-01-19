package com.danielvm.destiny2bot.factory.creator;

import com.danielvm.destiny2bot.dto.discord.Attachment;
import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Embedded;
import com.danielvm.destiny2bot.dto.discord.EmbeddedAuthor;
import com.danielvm.destiny2bot.dto.discord.EmbeddedImage;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.enums.InteractionResponseType;
import com.danielvm.destiny2bot.enums.Raid;
import com.danielvm.destiny2bot.enums.RaidEncounter;
import com.danielvm.destiny2bot.service.ImageAssetService;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class RaidMapMessageCreator implements ApplicationCommandSource, AutocompleteSource {

  private static final String RAID_OPTION_NAME = "raid";
  private static final String EMPTY_RAID_OPTION_MESSAGE = "Please specify a 'raid' option prior to selecting encounter";
  private static final String EMPTY_RAID_OPTION_VALUE = "No raid selected";

  private final InteractionResponse defaultErrorResponse = InteractionResponse.builder()
      .type(InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType())
      .data(InteractionResponseData.builder()
          .choices(List.of(new Choice(EMPTY_RAID_OPTION_MESSAGE, EMPTY_RAID_OPTION_VALUE)))
          .build())
      .build();

  private final ImageAssetService imageAssetService;

  public RaidMapMessageCreator(ImageAssetService imageAssetService) {
    this.imageAssetService = imageAssetService;
  }

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    try {
      return imageAssetService.retrieveEncounterImages(interaction)
          .map(map -> map.entrySet().stream()
              .map(entry -> {
                try {
                  return Attachment.builder()
                      .id(entry.getKey())
                      .filename(entry.getValue().getFilename())
                      .size(Math.toIntExact(entry.getValue().contentLength()))
                      .build();
                } catch (IOException e) {
                  throw new RuntimeException(e);
                }
              })
              .toList())
          .map(attachments -> {
            List<Embedded> embedds = attachments.stream()
                .map(attachment -> Embedded.builder()
                    .type("image")
                    .author(
                        EmbeddedAuthor.builder()
                            .name("Infographs powered by @a-phantom-moon")
                            .url("https://www.deviantart.com/a-phantom-moon/")
                            .build()
                    )
                    .image(EmbeddedImage.builder()
                        .url("attachment://" + attachment.getFilename())
                        .build())
                    .build())
                .toList();
            InteractionResponseData data = InteractionResponseData.builder()
                .attachments(attachments)
                .embeds(embedds)
                .build();
            return InteractionResponse.builder()
                .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
                .data(data)
                .build();
          });
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Mono<InteractionResponse> autocompleteResponse(Interaction interaction) {
    return Flux.fromIterable(interaction.getData().getOptions())
        .filter(option -> Objects.equals(option.getName(), RAID_OPTION_NAME) &&
                          Objects.equals(option.getType(), 3)) // 3 is for String data type
        .flatMap(option -> Flux.fromIterable(Arrays.asList(Raid.values()))
            .filter(raid -> raid.getLabel().equalsIgnoreCase(String.valueOf(option.getValue())))
            .next())
        .flatMap(raid -> Mono.just(raid)
            .flatMapMany(RaidEncounter::getRaidEncounters)
            .map(
                encounter -> new Choice(encounter.getEncounterName(), encounter.getDirectoryName()))
        )
        .collectList()
        .map(encounters -> {
          if (CollectionUtils.isEmpty(encounters)) {
            return defaultErrorResponse;
          }
          return InteractionResponse.builder()
              .type(InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType())
              .data(InteractionResponseData.builder()
                  .choices(encounters)
                  .build())
              .build();
        });
  }
}
