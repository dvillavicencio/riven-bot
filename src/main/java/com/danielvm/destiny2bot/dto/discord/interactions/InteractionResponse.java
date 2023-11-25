package com.danielvm.destiny2bot.dto.discord.interactions;

import com.danielvm.destiny2bot.enums.InteractionResponseEnum;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InteractionResponse {

    /**
     * The type of the InteractionResponse
     * <br>
     * {@see {@link InteractionResponseEnum }}
     */
    private Integer type;

    /**
     * Data attached to the Interaction Response
     */
    private InteractionResponseData data;
}
