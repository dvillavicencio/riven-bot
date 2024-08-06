package com.deahtstroke.rivenbot.handler.about;

import static com.deahtstroke.rivenbot.util.MessageUtils.BOT_INVITE_LINK;
import static com.deahtstroke.rivenbot.util.MessageUtils.DISCORD_SERVER;
import static com.deahtstroke.rivenbot.util.MessageUtils.GITHUB_REPO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.deahtstroke.rivenbot.dto.discord.Embedded;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedField;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedFooter;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.dto.discord.MessageComponent;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.handler.about.AboutHandler;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.info.BuildProperties;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AboutHandlerTest {

  @Mock
  BuildProperties buildProperties;

  @InjectMocks
  AboutHandler sut;

  @Test
  @DisplayName("Serving a request is successful")
  void shouldServeRequestSuccessfully() {
    // given: an interaction
    Interaction interaction = new Interaction();

    List<EmbeddedField> expectedFields = List.of(
        EmbeddedField.builder()
            .name("Usage Instructions")
            .value("""
                Here is a list of all available commands to use with Riven:
                - `/weekly_dungeon` - Gives info about the current weekly dungeon
                - `/weekly_raid` - Gives info about the current weekly raid
                - `/raid_stats` - Retrieves statistics for a Destiny 2 player using their \
                Bungie.net ID, the expected format for the username should be in the form *username#tag*""")
            .build(),
        EmbeddedField.builder()
            .name("Contribution & Feedback")
            .value("""
                Want to contribute? Check out Riven's [GitHub repo](%s)
                Provide feedback or suggest features in [Riven's Discord server](%s)
                """.formatted(GITHUB_REPO, DISCORD_SERVER))
            .build(),
        EmbeddedField.builder()
            .name("Permissions")
            .value("Requires permission to read and send messages in a Discord server")
            .build(),
        EmbeddedField.builder()
            .name("Developer Information")
            .value("""
                Created by Deahtstroke
                Github Repository: https://github.com/dvillavicencio/riven-bot
                """)
            .build());

    when(buildProperties.getVersion()).thenReturn("1.0.0");
    when(buildProperties.getName()).thenReturn("RivenBot");

    // when: resolve() is invoked
    StepVerifier.create(sut.serve(interaction))
        // then: the response has the expected content
        .assertNext(response -> {
          assertThat(response).isNotNull();
          assertThat(response.getType()).isEqualTo(
              InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType());
          InteractionResponseData data = response.getData();
          assertThat(data).isNotNull();
          assertThat(data.getEmbeds()).hasSize(1);

          Embedded firstEmbedded = data.getEmbeds().getFirst();
          assertThat(firstEmbedded.getDescription()).isEqualTo("""
              Riven of a Thousand Servers is a Discord application designed to give Destiny 2 players \
              useful utilities accessed by typing slash-commands. Some features include information on \
              weekly dungeons and weekly raids in rotation, as well as \
              exhaustive raid statistics for players.""");
          assertThat(firstEmbedded.getFields()).containsAll(expectedFields);

          EmbeddedFooter footer = firstEmbedded.getFooter();
          assertThat(footer.getText()).isEqualTo(
              "Current Version %s:%s".formatted(buildProperties.getName(),
                  buildProperties.getVersion()));

          List<MessageComponent> components = data.getComponents();
          assertThat(components).hasSize(1);

          MessageComponent firstActionRow = components.getFirst();
          assertThat(firstActionRow.getComponents()).hasSize(1);

          MessageComponent linkButton = firstActionRow.getComponents().get(0);
          assertThat(linkButton.getType()).isEqualTo(2);
          assertThat(linkButton.getLabel()).isEqualTo("Add to Server");
          assertThat(linkButton.getStyle()).isEqualTo(5);
          assertThat(linkButton.getUrl()).isEqualTo(BOT_INVITE_LINK);
        }).verifyComplete();
  }
}
