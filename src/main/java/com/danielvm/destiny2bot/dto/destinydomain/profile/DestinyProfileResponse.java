package com.danielvm.destiny2bot.dto.destinydomain.profile;

import com.danielvm.destiny2bot.dto.BaseApiResponse;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class DestinyProfileResponse implements BaseApiResponse {

    @JsonAlias("Response")
    private DestinyProfileDto response;
}
