package com.danielvm.destiny2bot.factory.creator;

import com.danielvm.destiny2bot.dto.discord.Component;
import com.danielvm.destiny2bot.dto.discord.Embedded;
import com.danielvm.destiny2bot.dto.discord.EmbeddedImage;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.dto.discord.Option;
import com.danielvm.destiny2bot.dto.discord.SelectOption;
import com.danielvm.destiny2bot.enums.Raid;
import com.danielvm.destiny2bot.exception.ResourceNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import reactor.core.publisher.Mono;

@org.springframework.stereotype.Component
public class RaidDiagramMessageCreator implements ApplicationCommandSource, MessageComponentSource {

  @Override
  public Mono<InteractionResponse> createResponse(Interaction interaction) {
    Option raidOption = interaction.getData().getOptions().stream()
        .filter(opt -> Objects.equals(opt.getName(), "raid"))
        .findFirst()
        .orElseThrow(
            () -> new ResourceNotFoundException("No raid option found for given interaction"));
    Raid raid = Arrays.stream(Raid.values())
        .filter(r -> Objects.equals(raidOption.getValue(), r.name()))
        .findFirst()
        .orElseThrow(() -> new ResourceNotFoundException(
            "No raid found for the given option selected in 'Raid' field")
        );

    return raid.getEncounters()
        .map(encounter -> SelectOption.builder()
            .label(encounter.getEncounterName())
            .value(encounter.getEncounterName())
            .description(encounter.getDescription())
            .build())
        .collectList()
        .map(selectOptions -> new InteractionResponse(4,
            InteractionResponseData.builder()
                .content("The following are the encounters for " + raid.getLabel())
                .components(List.of(
                    Component.builder()
                        .type(1)
                        .components(
                            List.of(Component.builder()
                                .customId("select_raid_encounter")
                                .type(3)
                                .options(selectOptions)
                                .build()))
                        .build()))
                .build()));
  }

  @Override
  public Mono<InteractionResponse> messageComponentResponse(Interaction interaction) {
    String encounterName = interaction.getData().getValues().getFirst();
    EmbeddedImage arenaImage = EmbeddedImage.builder()
        .height(360).width(360)
        .url(
            "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/39d416b6-bd7e-4aa2-ac85-a802ba401781/degan7g-f73639b1-cb8b-487a-b348-e221682847a7.png/v1/fit/w_828,h_978,q_70,strp/ii_ii_kalli__the_corrupted___damage_phase_by_a_phantom_moon_degan7g-414w-2x.jpg?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7ImhlaWdodCI6Ijw9MTUxMSIsInBhdGgiOiJcL2ZcLzM5ZDQxNmI2LWJkN2UtNGFhMi1hYzg1LWE4MDJiYTQwMTc4MVwvZGVnYW43Zy1mNzM2MzliMS1jYjhiLTQ4N2EtYjM0OC1lMjIxNjgyODQ3YTcucG5nIiwid2lkdGgiOiI8PTEyODAifV1dLCJhdWQiOlsidXJuOnNlcnZpY2U6aW1hZ2Uub3BlcmF0aW9ucyJdfQ.3SsqOGb9VRx2qCYjS_UNUsLJ5TWaZt8HjF8RnUxc7vw")
        .build();
    Embedded raidEncounter = Embedded.builder()
        .description("Here's a Map of the %s encounter".formatted(encounterName))
        .image(arenaImage)
        .build();
    return Mono.just(new InteractionResponse(7,
        InteractionResponseData.builder()
            .embeds(List.of(raidEncounter))
            .build()));
  }
}
