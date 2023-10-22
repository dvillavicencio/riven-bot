package com.danielvm.destiny2bot.dto.destinydomain.profile;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DestinyProfileDto {

    private CharacterDataDto characters;

    private CharacterInventoriesDto characterInventories;

    @Data
    private static class CharacterDataDto {
        private Map<String, CharacterInfoDto> data;
    }

    @Data
    private static class CharacterInfoDto {
        private String membershipId;
        private String characterId;

    }

    @Data
    private static class CharacterInventoriesDto {
        private Map<String, ItemInfoDto> data;
    }

    @Data
    private static class ItemInfoDto {
        private List<ItemInfo> items;
    }

    @Data
    private static class ItemInfo {

        private Long itemHash;
        private String itemInstanceId;
        private Long bucketHash;
    }
}
