package com.danielvm.destiny2bot.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.dto.discord.Option;
import com.danielvm.destiny2bot.enums.InteractionResponseType;
import com.danielvm.destiny2bot.enums.InteractionType;
import com.danielvm.destiny2bot.enums.Raid;
import com.danielvm.destiny2bot.enums.RaidEncounter;
import com.danielvm.destiny2bot.factory.creator.RaidMapMessageCreator;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class RaidMapMessageCreatorTest {

  @InjectMocks
  private RaidMapMessageCreator sut;

  @Test
  @DisplayName("Creating autocomplete response works correctly")
  public void creatingAutocompleteResponseWorksCorrectly() {
    // given: an interaction with a 'raid' option value present
    List<Option> options = List.of(new Option("raid", 3, "LAST_WISH", false));
    InteractionData data = InteractionData.builder()
        .id(1).name("raid_map").options(options).type(1)
        .build();
    Interaction interaction = Interaction.builder()
        .id(1).data(data).type(InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE.getType())
        .build();

    List<Choice> choices = RaidEncounter.getRaidEncounters(Raid.LAST_WISH)
        .map(encounter -> new Choice(encounter.getEncounterName(), encounter.getDirectoryName()))
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
  @DisplayName("Creating autocomplete response fails if no raid option is passed")
  public void creatingAutocompleteResponseFailsForEmptyRaidOption() {
    // given: an interaction with a 'raid' option value present
    List<Option> options = Collections.emptyList();
    InteractionData data = InteractionData.builder()
        .id(1).name("raid_map").options(options).type(1)
        .build();
    Interaction interaction = Interaction.builder()
        .id(1).data(data).type(InteractionType.APPLICATION_COMMAND_AUTOCOMPLETE.getType())
        .build();

    // when: the autocomplete request is received
    var response = StepVerifier.create(sut.autocompleteResponse(interaction));

    // then: the expected output should have an option to please fill in the 'raid' option first
    response.assertNext(output -> {
      assertThat(output.getType())
          .isEqualTo(InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType());
      assertThat(output.getData().getChoices().get(0).getName())
          .isEqualTo("Please specify a 'raid' option prior to selecting encounter");
      assertThat(output.getData().getChoices().get(0).getValue())
          .isEqualTo("No raid selected");
    }).verifyComplete();
  }

}
