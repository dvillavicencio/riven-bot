package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.enums.InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE;
import static com.danielvm.destiny2bot.factory.handler.WeeklyDungeonHandler.MESSAGE_TEMPLATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.dto.discord.DiscordUser;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.dto.discord.Member;
import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.factory.ApplicationCommandFactory;
import com.danielvm.destiny2bot.factory.AutocompleteFactory;
import com.danielvm.destiny2bot.factory.handler.WeeklyDungeonHandler;
import com.danielvm.destiny2bot.util.MessageUtil;
import java.time.LocalDate;
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
  WeeklyDungeonHandler weeklyDungeonHandler;

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
        .thenReturn(weeklyDungeonHandler);

    when(weeklyDungeonHandler.createResponse(interaction))
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

}
