package com.danielvm.destiny2bot.dto.destinydomain.membership;

import lombok.Data;

import java.util.List;

@Data
public class DestinyMembershipDto {

    /**
     * List of Destiny memberships for a user
     */
    private List<DestinyMembershipsDto> destinyMemberships;
}
