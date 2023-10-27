package com.danielvm.destiny2bot.dto.destiny.profile;

import lombok.Data;

@Data
public class ItemInfoDto {

    private Long itemHash;
    private String itemInstanceId;
    private Long bucketHash;
}