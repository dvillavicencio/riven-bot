package com.danielvm.destiny2bot.dto.discord.interactions;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InteractionResponseData {

    /**
     * The message content of the InteractionResponse
     */
    private String content;

    /**
     * List of embeds{@link }
     */
    private List<Embedded> embeds;
}
