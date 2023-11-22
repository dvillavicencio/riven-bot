package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.context.UserIdentityContext;
import com.danielvm.destiny2bot.dto.discord.interactions.*;
import com.danielvm.destiny2bot.enums.InteractionType;
import com.danielvm.destiny2bot.util.OAuth2Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static com.danielvm.destiny2bot.enums.InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE;
import static com.danielvm.destiny2bot.enums.InteractionType.*;

@Service
@Slf4j
public class InteractionService {

    private final DiscordConfiguration discordConfiguration;
    private final CharacterWeaponsService characterWeaponsService;

    public InteractionService(
            DiscordConfiguration discordConfiguration,
            CharacterWeaponsService characterWeaponsService) {
        this.discordConfiguration = discordConfiguration;
        this.characterWeaponsService = characterWeaponsService;
    }

    /**
     * Handles the incoming interactions
     *
     * @param interaction The received interaction from Discord chat
     * @return {@link InteractionResponse}
     */
    public InteractionResponse handleInteraction(Interaction interaction) {
        if (Objects.equals(findByValue(interaction.getType()), APPLICATION_COMMAND)) {
            if (interaction.getData().getName().equals("authorize")) {
                return InteractionResponse.builder()
                        .type(CHANNEL_MESSAGE_WITH_SOURCE.getType())
                        .data(InteractionResponseData.builder()
                                .content("""
                                        Before using the Armory Bot you will need to register your Bungie Account
                                        and link it to your Discord account to be able to pull weapons from your Vault.
                                                                                
                                        Please use the link below""")
                                .embeds(List.of(
                                        Embedded.builder()
                                                .title("Register Here")
                                                .description("This registration link will redirect you to authorize the bot")
                                                .url(buildRegistrationLink())
                                                .build())
                                ).build()
                        )
                        .build();

            } else if (interaction.getData().getName().equals("weapons")) {
                characterWeaponsService.getVaultWeapons();
                return null;
            }
        } else if (Objects.equals(findByValue(interaction.getType()), PING)) {
            return InteractionResponse.builder()
                    .type(PING.getType())
                    .build();
        }
        return null;
    }

    private String buildRegistrationLink() {
        return UriComponentsBuilder.fromHttpUrl(discordConfiguration.getAuthorizationUrl())
                .queryParam(OAuth2Params.CLIENT_ID, discordConfiguration.getClientId())
                .queryParam(OAuth2Params.REDIRECT_URI, URLEncoder.encode(discordConfiguration.getCallbackUrl(),
                        StandardCharsets.UTF_8))
                .queryParam(OAuth2Params.RESPONSE_TYPE, OAuth2Params.CODE)
                .queryParam(OAuth2Params.SCOPE, discordConfiguration.getScopes())
                .build().toString();
    }
}
