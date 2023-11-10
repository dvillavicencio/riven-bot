package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.client.BungieProfileClient;
import com.danielvm.destiny2bot.dto.CharacterVault;
import com.danielvm.destiny2bot.dto.CharacterWeapon;
import com.danielvm.destiny2bot.dto.CharacterWeaponsResponse;
import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.character.vaultitems.VaultItem;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import com.danielvm.destiny2bot.enums.EntityTypeEnum;
import com.danielvm.destiny2bot.enums.ItemSubTypeEnum;
import com.danielvm.destiny2bot.enums.ItemTypeEnum;
import com.danielvm.destiny2bot.mapper.CharacterWeaponMapper;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.MappingTarget;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.function.Function;

import static com.danielvm.destiny2bot.enums.EntityTypeEnum.ITEM_INVENTORY_DEFINITION;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterWeaponsService {

    private static final String IMAGE_URL_ROOT = "http://www.bungie.net";

    private final BungieProfileClient bungieProfileClient;
    private final MembershipService membershipService;
    private final CharacterWeaponMapper weaponMapper;
    private final BungieManifestClient bungieManifestClient;

    /**
     * Get the weapons per character for the current user
     *
     * @param authentication The authentication object holding security details
     * @return {@link CharacterWeaponsResponse}
     */
    public CharacterVault getVaultWeapons(Authentication authentication) throws Exception {
        var membershipInfo = membershipService.getCurrentUserMembershipInformation(authentication);
        var membershipId = MembershipUtil.extractMembershipId(membershipInfo);
        var membershipType = MembershipUtil.extractMembershipType(membershipInfo);

        var vaultWeapons = bungieProfileClient.getCharacterVaultItems(
                AuthenticationUtil.getBearerToken(authentication), membershipType, membershipId);

        return CharacterVault.builder()
                .weapons(Objects.requireNonNull(vaultWeapons.getBody()).getResponse()
                        .getProfileInventory().getData().getItems().stream()
                        .map(weaponMapper::entityToWeapon)
                        .toList()).build();
    }

    public Mono<CharacterVault> getVaultWeaponsRx(Authentication authentication) throws Exception {
        return membershipService.getCurrentUserMembershipInformationRx(authentication)
                .flatMap(membershipResponse -> {
                    var membershipId = MembershipUtil.extractMembershipId(membershipResponse);
                    var membershipType = MembershipUtil.extractMembershipType(membershipResponse);
                    return bungieProfileClient.getCharacterVaultItemsRx(
                            AuthenticationUtil.getBearerToken(authentication), membershipType, membershipId);
                })
                .flatMapMany(items -> Flux.fromIterable(items.getResponse().getProfileInventory().getData().getItems()))
                .flatMap(item -> {
                            CharacterWeapon weapon = new CharacterWeapon();
                            return bungieManifestClient.getManifestEntityRx(
                                            ITEM_INVENTORY_DEFINITION.getId(), item.getItemHash())
                                    .map(entity -> {
                                        var e = entity.getResponse();
                                        Assert.notNull(e, "The response for item [%s] for hash [%s] cannot be null"
                                                .formatted(ITEM_INVENTORY_DEFINITION, item.getItemHash()));
                                        boolean isWeapon = Objects.equals(ItemTypeEnum.findByCode(e.getItemType()), ItemTypeEnum.WEAPON);
                                        if (isWeapon) {
                                            weapon.setWeaponType(ItemSubTypeEnum.findById(e.getItemSubType()));
                                            weapon.setWeaponName(e.getDisplayProperties().getName());
                                            weapon.setWeaponIcon(e.getDisplayProperties().getHasIcon() ?
                                                    IMAGE_URL_ROOT + e.getDisplayProperties().getIcon() : null);
                                        }
                                        return weapon;
                                    });
                        }
                )
                .collectList()
                .map(list -> CharacterVault.builder()
                        .weapons(list).build());
    }
}
