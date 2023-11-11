package com.danielvm.destiny2bot.dto.destiny.character.vaultitems;

import lombok.Data;

@Data
public class VaultItem {

    private String itemHash;

    private String itemInstanceId;

    private String bucketHash;

    private Integer quantity;
}
