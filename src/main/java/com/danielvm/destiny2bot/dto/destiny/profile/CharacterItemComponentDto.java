package com.danielvm.destiny2bot.dto.destiny.profile;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CharacterItemComponentDto {

    private CharacterInventoriesDto characterInventories;

    @Data
    public static class CharacterInventoriesDto {
        private Map<String, CharacterItemDto> data;
    }

    @Data
    public static class CharacterItemDto {
        private List<ItemInfoDto> items;
    }
}
