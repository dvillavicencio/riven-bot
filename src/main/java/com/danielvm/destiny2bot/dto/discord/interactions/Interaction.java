package com.danielvm.destiny2bot.dto.discord.interactions;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Interaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 315485352844067387L;

    /**
     * The Id of the interaction
     */
    private Object id;

    /**
     * The Id of the application
     */
    @JsonAlias("application_id")
    private Object applicationId;

    /**
     * The type of the interaction (see {@link com.danielvm.destiny2bot.enums.InteractionType}
     */
    private Integer type;

    /**
     * Additional data of the interaction, will be attached to all interactions besides PING
     */
    private InteractionData data;
}
