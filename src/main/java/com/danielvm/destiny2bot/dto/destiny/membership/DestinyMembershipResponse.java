package com.danielvm.destiny2bot.dto.destiny.membership;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class DestinyMembershipResponse {

    @JsonAlias("Response")
    private DestinyMembershipDto response;
}
