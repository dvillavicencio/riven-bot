package com.danielvm.destiny2bot.factory;

import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.dto.DestinyCharacter;
import com.danielvm.destiny2bot.dto.discord.Choice;
import com.danielvm.destiny2bot.dto.discord.DiscordUser;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.dto.discord.Member;
import com.danielvm.destiny2bot.service.DestinyCharacterService;
import java.util.List;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ExtendWith(MockitoExtension.class)
public class UserCharacterMessageCreatorTest {

  @Mock
  DestinyCharacterService destinyCharacterService;

  @InjectMocks
  UserCharacterMessageCreator sut;

  @Test
  @DisplayName("Create message is successful for users with more than one character")
  public void createMessageIsSuccessful() {
    // given: data from character service and userId
    String userId = "someUserId";
    DiscordUser user = new DiscordUser(userId, "deahtstroke");
    Interaction interaction = new Interaction(null, null, null, null, new Member(user));
    List<DestinyCharacter> characters = List.of(
        new DestinyCharacter("1", "Titan", 1890, "Human"),
        new DestinyCharacter("2", "Warlock", 1890, "Awoken"),
        new DestinyCharacter("3", "Hunter", 1890, "Exo")
    );

    when(destinyCharacterService.getCharactersForUser(userId)).thenReturn(
        Flux.fromIterable(characters));

    // when: createMessage is called
    FirstStep<InteractionResponse> response = StepVerifier.create(sut.commandResponse(interaction));

    // then: the created Discord interaction has correct fields and the 'all' choice is added
    List<Choice> expectedChoices = characters.stream()
        .map(ch -> new Choice("[%s] %s - %s".formatted(ch.getLightLevel(), ch.getCharacterRace(),
            ch.getCharacterClass()), ch.getCharacterId()))
        .collect(Collectors.toList());
    // Add the all flag
    expectedChoices.add(new Choice("All", "Gets stats for all characters"));

    response
        .assertNext(interactionResponse -> {
          Assertions.assertThat(interactionResponse.getType()).isEqualTo(8);
          Assertions.assertThat(interactionResponse.getData()).isNotNull();
          Assertions.assertThat(interactionResponse.getData().getChoices().size()).isEqualTo(4);
          Assertions.assertThat(interactionResponse.getData().getChoices())
              .containsExactlyElementsOf(expectedChoices);
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Create message works for players with only one character")
  public void createMessageForPlayersWithOneCharacter() {
    // given: data from character service and userId
    String userId = "someUserId";
    DiscordUser user = new DiscordUser(userId, "deahtstroke");
    Interaction interaction = new Interaction(null, null, null, null, new Member(user));
    List<DestinyCharacter> characters = List.of(
        new DestinyCharacter("1", "Titan", 1890, "Human")
    );

    when(destinyCharacterService.getCharactersForUser(userId)).thenReturn(
        Flux.fromIterable(characters));

    // when: createMessage is called
    FirstStep<InteractionResponse> response = StepVerifier.create(sut.commandResponse(interaction));

    // then: the created Discord interaction has correct fields but no 'all' choice is added
    List<Choice> expectedChoices = characters.stream()
        .map(ch -> new Choice("[%s] %s - %s".formatted(ch.getLightLevel(), ch.getCharacterRace(),
            ch.getCharacterClass()), ch.getCharacterId()))
        .collect(Collectors.toList());

    response
        .assertNext(interactionResponse -> {
          Assertions.assertThat(interactionResponse.getType()).isEqualTo(8);
          Assertions.assertThat(interactionResponse.getData()).isNotNull();
          Assertions.assertThat(interactionResponse.getData().getChoices().size()).isEqualTo(1);
          Assertions.assertThat(interactionResponse.getData().getChoices())
              .containsExactlyElementsOf(expectedChoices);
        })
        .verifyComplete();
  }

}
