package com.deahtstroke.rivenbot.handler.raidstats;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionData;
import com.deahtstroke.rivenbot.dto.discord.Option;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.processor.AsyncRaidsProcessor;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class RaidStatsCommandHandlerTest {

  @Mock
  AsyncRaidsProcessor asyncRaidsProcessor;

  @InjectMocks
  RaidStatsCommandHandler sut;

  @Test
  @DisplayName("Serve should work successfully")
  void shouldServeRaidStatsSuccessfully() {
    // given: an interaction with a Bungie user
    List<Option> options = List.of(
        new Option("username", 1, "Deaht#3180", false, Collections.emptyList()));
    Interaction interaction = Interaction.builder()
        .data(InteractionData.builder()
            .options(options)
            .build())
        .token("someToken")
        .build();

    when(asyncRaidsProcessor.processRaidsAsync("Deaht", "3180", "someToken"))
        .thenReturn(Mono.empty());

    // when: serve() is invoked
    StepVerifier.create(sut.serve(interaction))
        .assertNext(response -> {
          assertThat(response.getType())
              .isEqualTo(InteractionResponseType.DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE.getType());
          assertThat(response.getData()).isNotNull();
        }).verifyComplete();
  }
}
