package com.deahtstroke.rivenbot.handler;

import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

import com.deahtstroke.rivenbot.dispatcher.DiscordInteractionDispatcher;
import com.deahtstroke.rivenbot.dto.discord.DiscordUser;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionData;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.dto.discord.Member;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.handler.weeklydungeon.WeeklyDungeonHandler;
import com.deahtstroke.rivenbot.util.MessageUtils;
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
class InteractionHandlerTest {

  WebTestClient webTestClient;

  @Mock
  InteractionFactory interactionFactory;

  @BeforeEach
  public void setup() {
    DiscordInteractionDispatcher handler = new DiscordInteractionDispatcher(interactionFactory);
    RouterFunction<?> route = RouterFunctions.route()
        .route(POST("/interactions"), handler::resolveRequest).build();
    webTestClient = WebTestClient.bindToRouterFunction(route).build();
  }

  @Test
  @DisplayName("Create response is successful for PING interaction request")
  void handleInteractionForPingRequest() {
    // given: interaction data
    Interaction interaction = Interaction.builder()
        .applicationId("myApplicationId")
        .type(1)
        .build();

    when(interactionFactory.serve(interaction))
        .thenReturn(Mono.just(InteractionResponse.pingResponse()));

    // when: the interaction is received
    // then: the response body is correct
    webTestClient.post().uri("/interactions")
        .body(BodyInserters.fromValue(interaction))
        .exchange()
        .expectStatus().is2xxSuccessful()
        .expectBody()
        .jsonPath("$.type").isEqualTo(1)
        .jsonPath("$.data").isEmpty();
  }

  @Test
  @DisplayName("serve() successful for weekly_dungeon slash-command")
  void handleInteractionForWeeklyDungeon() {
    // given: interaction data from an application command (slash command)
    InteractionData data = InteractionData.builder().id("someId").name("weekly_dungeon").type(1)
        .build();
    Member member = new Member(new DiscordUser("someId", "myUsername"));
    Interaction interaction = Interaction.builder()
        .applicationId("myApplicationId")
        .type(2)
        .member(member).data(data)
        .build();

    String endDate = MessageUtils.formatDate(LocalDate.now());
    String dungeon = "Duality";
    String content = WeeklyDungeonHandler.MESSAGE_TEMPLATE.formatted(dungeon, endDate);

    InteractionResponse message = InteractionResponse.builder()
        .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(InteractionResponseData.builder()
            .content(content)
            .build())
        .build();

    when(interactionFactory.serve(interaction))
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
