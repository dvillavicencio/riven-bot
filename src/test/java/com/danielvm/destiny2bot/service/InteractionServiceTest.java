package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.InteractionResponseType.APPLICATION_COMMAND_AUTOCOMPLETE_RESULT;
import static com.danielvm.destiny2bot.enums.InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE;
import static com.danielvm.destiny2bot.factory.creator.WeeklyDungeonMessageCreator.MESSAGE_TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.DiscordUser;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.dto.discord.Member;
import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.factory.ApplicationCommandFactory;
import com.danielvm.destiny2bot.factory.AutocompleteFactory;
import com.danielvm.destiny2bot.factory.creator.RaidStatsMessageCreator;
import com.danielvm.destiny2bot.factory.creator.WeeklyDungeonMessageCreator;
import com.danielvm.destiny2bot.util.MessageUtil;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ExtendWith(MockitoExtension.class)
public class InteractionServiceTest {


  @Mock
  RaidStatsMessageCreator raidStatsMessageCreator;

  @Mock
  WeeklyDungeonMessageCreator weeklyDungeonMessageCreator;

  @Mock
  ApplicationCommandFactory applicationCommandFactory;

  @Mock
  AutocompleteFactory autocompleteFactory;

  @InjectMocks
  private InteractionService interactionService;

  @Test
  @DisplayName("Create response is successful for PING interaction request")
  public void handleInteractionForPingRequest() {
    // given: interaction data
    Interaction interaction = Interaction.builder()
        .applicationId("myApplicationId").type(1)
        .build();

    // when: the interaction is received
    FirstStep<InteractionResponse> response = StepVerifier.create(
        interactionService.handleInteraction(interaction));

    // then: the response received is correct
    response
        .assertNext(result -> assertThat(result.getType()).isEqualTo(1))
        .verifyComplete();
  }

  @Test
  @DisplayName("Create response is successful for APPLICATION_COMMAND interaction request that doesn't require authorization")
  public void handleInteractionFor() {
    // given: interaction data from an application command (slash command)
    InteractionData data = InteractionData.builder().id("someId").name("weekly_dungeon").type(1)
        .build();
    Member member = new Member(new DiscordUser("someId", "myUsername"));
    Interaction interaction = Interaction.builder()
        .applicationId("myApplicationId").type(2).member(member).data(data)
        .build();

    String endDate = MessageUtil.formatDate(LocalDate.now());
    String dungeon = "Duality";
    String content = MESSAGE_TEMPLATE.formatted(dungeon, endDate);

    InteractionResponse message = InteractionResponse.builder()
        .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(InteractionResponseData.builder()
            .content(content)
            .build())
        .build();

    when(applicationCommandFactory.messageCreator(SlashCommand.WEEKLY_DUNGEON))
        .thenReturn(weeklyDungeonMessageCreator);

    when(weeklyDungeonMessageCreator.createResponse(interaction))
        .thenReturn(Mono.just(message));

    // when: the interaction is received
    FirstStep<InteractionResponse> response = StepVerifier.create(
        interactionService.handleInteraction(interaction));

    // then: the response received is correct and the message returned has the correct content
    response
        .assertNext(result -> {
          assertThat(result.getType()).isEqualTo(CHANNEL_MESSAGE_WITH_SOURCE.getType());
          assertThat(result.getData().getContent()).isEqualTo(content);
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Create response is successful for APPLICATION_COMMAND_AUTOCOMPLETE interaction request that requires authentication")
  public void handleInteractionForAuthorizedAutocomplete() {
    // given: interaction data from a slash-command autocomplete
    String userId = "userId";
    Member member = new Member(new DiscordUser(userId, "myUsername"));
    InteractionData data = InteractionData.builder()
        .id("someId").name("raid_stats").type(1)
        .build();
    Interaction interaction = Interaction.builder()
        .applicationId("myApplicationId").type(4).data(data).member(member)
        .build();

    List<Choice> choices = List.of(
        new Choice("Character 1", "1"),
        new Choice("Character 1", "1"),
        new Choice("Character 1", "1"),
        new Choice("All", "All")
    );
    InteractionResponse message = InteractionResponse.builder()
        .type(APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType())
        .data(InteractionResponseData.builder()
            .choices(choices)
            .build())
        .build();

    when(autocompleteFactory.messageCreator(SlashCommand.RAID_STATS))
        .thenReturn(raidStatsMessageCreator);

    when(raidStatsMessageCreator.autocompleteResponse(interaction))
        .thenReturn(Mono.just(message));

    // when: the interaction is received
    FirstStep<InteractionResponse> response = StepVerifier.create(
        interactionService.handleInteraction(interaction));

    // then: the response received is correct and the message returned has the correct content
    response
        .assertNext(result -> {
          assertThat(result.getType()).isEqualTo(APPLICATION_COMMAND_AUTOCOMPLETE_RESULT.getType());
          assertThat(result.getData().getChoices()).containsAll(choices);
        })
        .verifyComplete();
  }
}
