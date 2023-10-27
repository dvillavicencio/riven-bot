package com.danielvm.destiny2bot.dto.destiny.profile;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class CharacterItemResponse {

    @JsonAlias("Response")
    private CharacterItemComponentDto response;
}
