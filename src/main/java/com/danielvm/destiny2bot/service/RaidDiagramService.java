package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.discord.Component;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class RaidDiagramService implements MessageService {

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
                .content("The following are the encounters for " + raidOption.getName())
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
}
