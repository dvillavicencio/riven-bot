package com.danielvm.destiny2bot.dto.destiny.profile;

import lombok.Data;

import java.util.Map;

@Data
public class CharacterInfoComponentDto {

    private CharacterDataDto characters;
    @Data
    public static class CharacterDataDto {
        private Map<String, CharacterInfoDto> data;
    }
}
