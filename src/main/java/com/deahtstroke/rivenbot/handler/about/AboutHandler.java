package com.deahtstroke.rivenbot.handler.about;

import static com.deahtstroke.rivenbot.util.MessageUtils.DISCORD_SERVER;
import static com.deahtstroke.rivenbot.util.MessageUtils.GITHUB_REPO;
import static com.deahtstroke.rivenbot.util.MessageUtils.ICON_URL;

import com.deahtstroke.rivenbot.dto.discord.Embedded;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedField;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedFooter;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedThumbnail;
import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.enums.SlashCommand;
import com.deahtstroke.rivenbot.handler.SlashCommandHandler;
import com.deahtstroke.rivenbot.util.MessageComponents;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class AboutHandler implements SlashCommandHandler {

  private final BuildProperties buildProperties;
  private final String inviteLink;

  public AboutHandler(BuildProperties buildProperties,
      @Value("${application.inviteLink}") String inviteLink) {
    this.buildProperties = buildProperties;
    this.inviteLink = inviteLink;
  }

  @Override
  public SlashCommand getSlashCommand() {
    return SlashCommand.ABOUT;
  }

  @Override
  public Mono<InteractionResponse> serve(Interaction interaction) {
    Embedded aboutEmbed = Embedded.builder()
        .title("Riven of a Thousand Servers")
        .thumbnail(EmbeddedThumbnail.builder()
            .url(ICON_URL)
            .build())
        .description("""
            Riven of a Thousand Servers is a Discord application designed to give Destiny 2 players \
            useful utilities accessed by typing slash-commands. Some features include information on \
            weekly dungeons and weekly raids in rotation, as well as \
            exhaustive raid statistics for players.""")
        .fields(List.of(
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
                .build()))
        .footer(EmbeddedFooter.builder()
            .text("Current Version: %s:%s".formatted(buildProperties.getName(),
                buildProperties.getVersion()))
            .build())
        .build();

    InteractionResponseData data = InteractionResponseData.builder()
        .embeds(List.of(aboutEmbed))
        .components(MessageComponents.components()
            .addActionRow(MessageComponents.actionRow()
                .linkButton("Add to Server", inviteLink))
            .build())
        .build();
    return Mono.just(InteractionResponse.builder()
        .type(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType())
        .data(data).build());
  }
}
