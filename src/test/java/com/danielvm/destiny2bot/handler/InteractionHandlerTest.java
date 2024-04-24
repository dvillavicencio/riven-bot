package com.danielvm.destiny2bot.handler;

import static com.danielvm.destiny2bot.enums.InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE;
import static com.danielvm.destiny2bot.handler.WeeklyDungeonHandler.MESSAGE_TEMPLATE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

import com.danielvm.destiny2bot.dto.discord.DiscordUser;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.InteractionResponseData;
import com.danielvm.destiny2bot.dto.discord.Member;
import com.danielvm.destiny2bot.enums.SlashCommand;
import com.danielvm.destiny2bot.factory.ApplicationCommandFactory;
import com.danielvm.destiny2bot.factory.AutocompleteFactory;
import com.danielvm.destiny2bot.factory.MessageComponentFactory;
import com.danielvm.destiny2bot.service.RaidInfographicsService;
import com.danielvm.destiny2bot.util.MessageUtils;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
public class InteractionHandlerTest {

  WebTestClient webTestClient;

  @Mock
  ApplicationCommandFactory applicationCommandFactory;

  @Mock
  AutocompleteFactory autocompleteFactory;

  @Mock
  RaidInfographicsService raidInfographicsService;

  @Mock
  MessageComponentFactory messageComponentFactory;

  @BeforeEach
  public void setup() {
    InteractionHandler handler = new InteractionHandler(applicationCommandFactory,
        autocompleteFactory, messageComponentFactory, raidInfographicsService);
    RouterFunction<?> route = RouterFunctions.route()
        .route(POST("/interactions"), handler::handle).build();
    webTestClient = WebTestClient.bindToRouterFunction(route).build();
  }

  @Test
  @DisplayName("Create response is successful for PING interaction request")
  public void handleInteractionForPingRequest() {
    // given: interaction data
    Interaction interaction = Interaction.builder()
        .applicationId("myApplicationId").type(1)
        .build();

    // when: the interaction is received
    var response = webTestClient.post().uri("/interactions")
        .body(BodyInserters.fromValue(interaction))
        .exchange();

    // then: the response body is correct
    response.expectBody().jsonPath("$.type").isEqualTo(1);
    response.expectBody().jsonPath("$.data").isEmpty();
    response.expectStatus().is2xxSuccessful();
  }

  @Test
  @DisplayName("Create response is successful for weekly_dungeon slash-command")
  public void handleInteractionForWeeklyDungeon() {
    // given: interaction data from an application command (slash command)
    InteractionData data = InteractionData.builder().id("someId").name("weekly_dungeon").type(1)
        .build();
    Member member = new Member(new DiscordUser("someId", "myUsername"));
    Interaction interaction = Interaction.builder()
        .applicationId("myApplicationId")
        .type(2)
        .member(member).data(data)
        .build();

    WeeklyRaidHandler weeklyRaidHandler = mock(WeeklyRaidHandler.class);
    when(applicationCommandFactory.messageCreator(SlashCommand.WEEKLY_DUNGEON))
        .thenReturn(weeklyRaidHandler);

    String endDate = MessageUtils.formatDate(LocalDate.now());
    String dungeon = "Duality";
    String content = MESSAGE_TEMPLATE.formatted(dungeon, endDate);

    InteractionResponse message = InteractionResponse.builder()
        .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(InteractionResponseData.builder()
            .content(content)
            .build())
        .build();

    when(weeklyRaidHandler.createResponse(interaction))
        .thenReturn(Mono.just(message));

    // when: the interaction is received
    var response = webTestClient.post().uri("/interactions")
        .body(BodyInserters.fromValue(interaction))
        .exchange();

    response.expectStatus().is2xxSuccessful();
    response.expectBody().jsonPath("$.type").isEqualTo(4);
    response.expectBody().jsonPath("$.data").isNotEmpty();
    response.expectBody().jsonPath("$.data.content").isEqualTo(content);
  }

}
