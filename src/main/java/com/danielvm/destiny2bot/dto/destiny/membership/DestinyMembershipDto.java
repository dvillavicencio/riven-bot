package com.danielvm.destiny2bot.dto.destiny.membership;

import lombok.Data;

import java.util.List;

@Data
public class DestinyMembershipDto {

    /**
     * List of Destiny memberships for a user
     */
    private List<DestinyMembershipsDto> destinyMemberships;
}
