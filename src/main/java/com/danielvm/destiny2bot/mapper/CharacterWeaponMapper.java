package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.dto.CharacterWeapon;
import com.danielvm.destiny2bot.dto.destiny.character.vaultitems.VaultItem;
import com.danielvm.destiny2bot.enums.ItemSubTypeEnum;
import com.danielvm.destiny2bot.enums.ItemTypeEnum;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static com.danielvm.destiny2bot.enums.EntityTypeEnum.ITEM_INVENTORY_DEFINITION;

@Mapper(componentModel = "spring")
public abstract class CharacterWeaponMapper {

    private static final String IMAGE_URL_ROOT = "http://www.bungie.net";

    private BungieManifestClient bungieManifestClient;

    public abstract CharacterWeapon entityToWeapon(VaultItem item);

    @Autowired
    public void setBungieManifestClient(BungieManifestClient bungieManifestClient) {
        this.bungieManifestClient = bungieManifestClient;
    }
}
