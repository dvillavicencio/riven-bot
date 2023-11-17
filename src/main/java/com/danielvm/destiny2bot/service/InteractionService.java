package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.config.DiscordConfiguration;
import com.danielvm.destiny2bot.dto.discord.interactions.*;
import com.danielvm.destiny2bot.enums.InteractionType;
import com.danielvm.destiny2bot.util.OAuth2Params;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.danielvm.destiny2bot.enums.InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE;

@Service
@Slf4j
public class InteractionService {

    private final DiscordConfiguration discordConfiguration;

    public InteractionService(DiscordConfiguration discordConfiguration) {
        this.discordConfiguration = discordConfiguration;
    }

    /**
     * Handles the incoming interactions
     *
     * @param request   The incoming httpServletRequest
     * @param signature The signature of the interaction
     * @param timestamp The timestamp of the interaction
     */
    public InteractionResponse handleInteraction(Interaction interaction) {
        if (interaction.getType().equals(InteractionType.APPLICATION_COMMAND.getType())) {
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
                                                        .description("This is the registration link... duh")
                                                        .url(buildRegistrationLink())
                                                        .build(),
                                                Embedded.builder()
                                                        .title("Submission")
                                                        .image(EmbeddedImage.builder()
                                                                .url("http://www.bungie.net/common/destiny2_content/icons/4d596b18d607700aca914f348fa188f6.jpg")
                                                                .width(10)
                                                                .height(10)
                                                                .build()
                                                        )
                                                        .fields(List.of(
                                                                        EmbeddedField.builder().name("power")
                                                                                .value("1800")
                                                                                .inline(true)
                                                                                .build(),
                                                                        EmbeddedField.builder()
                                                                                .name("Impact")
                                                                                .value("100")
                                                                                .inline(true)
                                                                                .build()
                                                                )
                                                        )
                                                        .build(),
                                        Embedded.builder()
                                                .title("Submission")
                                                .image(EmbeddedImage.builder()
                                                        .url("http://www.bungie.net/common/destiny2_content/icons/4d596b18d607700aca914f348fa188f6.jpg")
                                                        .width(10)
                                                        .height(10)
                                                        .build()
                                                )
                                                .fields(List.of(
                                                                EmbeddedField.builder().name("power")
                                                                        .value("1800")
                                                                        .inline(true)
                                                                        .build(),
                                                                EmbeddedField.builder()
                                                                        .name("Impact")
                                                                        .value("100")
                                                                        .inline(true)
                                                                        .build()
                                                        )
                                                )
                                                .build(),
                                        Embedded.builder()
                                                .title("Submission")
                                                .image(EmbeddedImage.builder()
                                                        .url("http://www.bungie.net/common/destiny2_content/icons/4d596b18d607700aca914f348fa188f6.jpg")
                                                        .width(10)
                                                        .height(10)
                                                        .build()
                                                )
                                                .fields(List.of(
                                                                EmbeddedField.builder().name("power")
                                                                        .value("1800")
                                                                        .inline(true)
                                                                        .build(),
                                                                EmbeddedField.builder()
                                                                        .name("Impact")
                                                                        .value("100")
                                                                        .inline(true)
                                                                        .build()
                                                        )
                                                )
                                                .build(),
                                        Embedded.builder()
                                                .title("Submission")
                                                .image(EmbeddedImage.builder()
                                                        .url("http://www.bungie.net/common/destiny2_content/icons/4d596b18d607700aca914f348fa188f6.jpg")
                                                        .width(10)
                                                        .height(10)
                                                        .build()
                                                )
                                                .fields(List.of(
                                                                EmbeddedField.builder().name("power")
                                                                        .value("1800")
                                                                        .inline(true)
                                                                        .build(),
                                                                EmbeddedField.builder()
                                                                        .name("Impact")
                                                                        .value("100")
                                                                        .inline(true)
                                                                        .build()
                                                        )
                                                )
                                                .build(),
                                        Embedded.builder()
                                                .title("Submission")
                                                .image(EmbeddedImage.builder()
                                                        .url("http://www.bungie.net/common/destiny2_content/icons/4d596b18d607700aca914f348fa188f6.jpg")
                                                        .width(10)
                                                        .height(10)
                                                        .build()
                                                )
                                                .fields(List.of(
                                                                EmbeddedField.builder().name("power")
                                                                        .value("1800")
                                                                        .inline(true)
                                                                        .build(),
                                                                EmbeddedField.builder()
                                                                        .name("Impact")
                                                                        .value("100")
                                                                        .inline(true)
                                                                        .build()
                                                        )
                                                )
                                                .build(),
                                        Embedded.builder()
                                                .title("Submission")
                                                .image(EmbeddedImage.builder()
                                                        .url("http://www.bungie.net/common/destiny2_content/icons/4d596b18d607700aca914f348fa188f6.jpg")
                                                        .width(10)
                                                        .height(10)
                                                        .build()
                                                )
                                                .fields(List.of(
                                                                EmbeddedField.builder().name("power")
                                                                        .value("1800")
                                                                        .inline(true)
                                                                        .build(),
                                                                EmbeddedField.builder()
                                                                        .name("Impact")
                                                                        .value("100")
                                                                        .inline(true)
                                                                        .build()
                                                        )
                                                )
                                                .build(),
                                        Embedded.builder()
                                                .title("Submission")
                                                .image(EmbeddedImage.builder()
                                                        .url("http://www.bungie.net/common/destiny2_content/icons/4d596b18d607700aca914f348fa188f6.jpg")
                                                        .width(10)
                                                        .height(10)
                                                        .build()
                                                )
                                                .fields(List.of(
                                                                EmbeddedField.builder().name("power")
                                                                        .value("1800")
                                                                        .inline(true)
                                                                        .build(),
                                                                EmbeddedField.builder()
                                                                        .name("Impact")
                                                                        .value("100")
                                                                        .inline(true)
                                                                        .build()
                                                        )
                                                )
                                                .build()
                                        )
                                )
                                .build()
                        )
                        .build();
            }
        }
        if (interaction.getType().equals(InteractionType.PING.getType())) {
            return InteractionResponse.builder()
                    .type(InteractionType.PING.getType())
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
