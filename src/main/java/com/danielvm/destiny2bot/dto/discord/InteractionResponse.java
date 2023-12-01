package com.danielvm.destiny2bot.dto.discord;

import com.danielvm.destiny2bot.enums.InteractionResponseEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
