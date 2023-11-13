package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.client.BungieManifestClientWrapper;
import com.danielvm.destiny2bot.dto.CharacterWeapon;
import com.danielvm.destiny2bot.dto.Stats;
import com.danielvm.destiny2bot.dto.destiny.character.vaultitems.VaultItem;
import com.danielvm.destiny2bot.enums.DamageTypeEnum;
import com.danielvm.destiny2bot.enums.ItemSubTypeEnum;
import com.danielvm.destiny2bot.enums.ItemTypeEnum;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Objects;

import static com.danielvm.destiny2bot.enums.EntityTypeEnum.ITEM_INVENTORY_DEFINITION;
import static com.danielvm.destiny2bot.enums.EntityTypeEnum.STAT_DEFINITION;

@Mapper(componentModel = "spring")
public abstract class VaultWeaponMapper {

    private static final String IMAGE_URL_ROOT = "http://www.bungie.net";

    private BungieManifestClientWrapper bungieManifestClient;

    public abstract CharacterWeapon entityToWeapon(VaultItem item);

    @AfterMapping
    public void toCharacterWeapon(VaultItem item, @MappingTarget CharacterWeapon weapon) {
        var inventoryEntity = bungieManifestClient.getManifestEntity(
                ITEM_INVENTORY_DEFINITION.getId(), item.getItemHash()).getBody().getResponse();
        Assert.notNull(inventoryEntity,
                "The response for item [%s] for hash [%s] cannot be null".formatted(ITEM_INVENTORY_DEFINITION, item.getItemHash()));

        boolean isWeapon = Objects.equals(ItemTypeEnum.findByCode(inventoryEntity.getItemType()), ItemTypeEnum.WEAPON);
        if (isWeapon) {
            var stats = inventoryEntity.getStats().getStats().entrySet().stream()
                    .map(stat -> {
                        var statName = bungieManifestClient.getManifestEntity(STAT_DEFINITION.getId(), stat.getKey())
                                .getBody().getResponse().getDisplayProperties().getName();
                        var statLevel = stat.getValue().getValue();
                        return new Stats(statName, statLevel);
                    }).toList();
            weapon.setStats(stats);
            weapon.setWeaponType(ItemSubTypeEnum.findById(inventoryEntity.getItemSubType()));
            weapon.setWeaponName(inventoryEntity.getDisplayProperties().getName());
            weapon.setWeaponIcon(inventoryEntity.getDisplayProperties().getHasIcon() ?
                    IMAGE_URL_ROOT + inventoryEntity.getDisplayProperties().getIcon() : null);
            weapon.setDamageType(DamageTypeEnum.findByCode(
                    Integer.valueOf(inventoryEntity.getDefaultDamageType())));
        }
    }

    @Autowired
    public void setBungieManifestClient(BungieManifestClientWrapper bungieManifestClient) {
        this.bungieManifestClient = bungieManifestClient;
    }
}
