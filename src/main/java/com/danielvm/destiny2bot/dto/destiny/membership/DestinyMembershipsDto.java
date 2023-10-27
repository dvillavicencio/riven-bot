package com.danielvm.destiny2bot.dto.destiny.membership;

import lombok.Data;

@Data
public class DestinyMembershipsDto {

    /**
     * The type of membership the current user:
     * <p>
     * See: <a href="https://bungie-net.github.io/multi/schema_BungieMembershipType.html#schema_BungieMembershipType">
     */
    private Integer membershipType;

    /**
     * The membership id of a user
     */
    private String membershipId;
}
