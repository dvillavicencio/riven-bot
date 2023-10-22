package com.danielvm.destiny2bot.dto.destinydomain.membership;

import com.danielvm.destiny2bot.dto.BaseApiResponse;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class DestinyMembershipResponse implements BaseApiResponse {

    @JsonAlias("Response")
    private DestinyMembershipDto response;
}
