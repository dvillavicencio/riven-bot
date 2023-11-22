package com.danielvm.destiny2bot.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserIdentity {

    /**
     * The user DiscordId
     */
    private String discordId;
}
