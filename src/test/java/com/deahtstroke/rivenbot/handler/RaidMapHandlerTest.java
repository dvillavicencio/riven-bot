package com.deahtstroke.rivenbot.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.TestUtils;
import com.deahtstroke.rivenbot.dto.discord.Attachment;
import com.deahtstroke.rivenbot.dto.discord.Choice;
import com.deahtstroke.rivenbot.dto.discord.Embedded;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedImage;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionData;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.dto.discord.Option;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.enums.InteractionType;
import com.deahtstroke.rivenbot.enums.Raid;
import com.deahtstroke.rivenbot.enums.RaidEncounter;
import com.deahtstroke.rivenbot.service.RaidInfographicsService;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class RaidMapHandlerTest {

  @Mock
  private RaidInfographicsService raidInfographicsService;

  @InjectMocks
  private RaidMapHandler sut;

  @Test
  @DisplayName("Creating autocomplete response works correctly")
  public void creatingAutocompleteResponseWorksCorrectly() {
    // given: an interaction with a 'raid' option value present
    List<Option> options = List.of(
        new Option("raid", 3, "last_wish", false, Collections.emptyList()));
    InteractionData data = InteractionData.builder()
        .id(1).name("raid_map").options(options).type(1)
        .build();
    Interaction interaction = Interaction.builder()
        .id(1L).data(data).type(InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE.getType())
        .build();

    List<Choice> choices = RaidEncounter.getRaidEncounters(Raid.LAST_WISH)
        .map(encounter -> new Choice(encounter.getName(), encounter.getDirectory()))
        .collectList().block();
    InteractionResponseData responseData = InteractionResponseData.builder()
        .choices(choices)
        .build();
    InteractionResponse expectedOutput = InteractionResponse.builder()
        .type(InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType())
        .data(responseData)
        .build();

    // when: the autocomplete request is received
    var response = StepVerifier.create(sut.autocompleteResponse(interaction));

    // then: the choices presented are correct according to the Raid
    response.assertNext(output -> {
      assertThat(output).isEqualTo(expectedOutput);
    }).verifyComplete();
  }

  @Test
  @DisplayName("Creating application command response works correctly")
  public void creatingApplicationCommandResponseWorksCorrectly() throws IOException {
    // given: an interaction with a 'raid' option value present
    List<Option> options = List.of(
        new Option("raid", 3, "last_wish", false, Collections.emptyList()),
        new Option("encounter", 3, "kalli", false, Collections.emptyList()));
    InteractionData data = InteractionData.builder()
        .id(1).name("raid_map").options(options).type(1)
        .build();
    Interaction interaction = Interaction.builder()
        .id(1L).data(data).type(InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE.getType())
        .build();

    Map<Long, Resource> resourcesMap = Map.of(
        1L, TestUtils.createResourceWithName("kalli-action-phase.jpg"),
        2L, TestUtils.createResourceWithName("kalli-dps-phase.jpg")
    );

    String raidName = "Last Wish";
    String raidEncounter = "Kalli, the Corrupted";
    String embedTitle = "Encounter maps for: %s at %s".formatted(raidName, raidEncounter);

    List<Embedded> expectedEmbeds = List.of(
        Embedded.builder()
            .title(embedTitle)
            .url("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            .image(EmbeddedImage.builder()
                .url("attachment://kalli-action-phase.jpg")
                .build())
            .type("image").build(),
        Embedded.builder()
            .title(embedTitle)
            .url("https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            .image(EmbeddedImage.builder()
                .url("attachment://kalli-dps-phase.jpg")
                .build())
            .type("image").build()
    );

    List<Attachment> expectedAttachments = resourcesMap.entrySet().stream()
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
        }).toList();

    Mockito.when(raidInfographicsService.retrieveEncounterImages(interaction))
        .thenReturn(Mono.just(resourcesMap));

    // when: the autocomplete request is received
    var response = StepVerifier.create(sut.createResponse(interaction));

    // then: the choices presented are correct according to the Raid
    response.assertNext(output -> {
      assertThat(output.getType()).isEqualTo(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType());
      assertThat(output.getData().getEmbeds()).containsAll(expectedEmbeds);
      assertThat(output.getData().getAttachments()).containsAll(expectedAttachments);
    }).verifyComplete();
  }

}
