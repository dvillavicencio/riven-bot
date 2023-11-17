package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieManifestClientWrapper;
import com.danielvm.destiny2bot.client.BungieProfileClient;
import com.danielvm.destiny2bot.dto.CharacterVault;
import com.danielvm.destiny2bot.dto.CharacterWeapon;
import com.danielvm.destiny2bot.dto.CharacterWeaponsResponse;
import com.danielvm.destiny2bot.dto.Stats;
import com.danielvm.destiny2bot.dto.destiny.character.vaultitems.VaultItem;
import com.danielvm.destiny2bot.enums.ItemSubTypeEnum;
import com.danielvm.destiny2bot.enums.ItemTypeEnum;
import com.danielvm.destiny2bot.mapper.VaultWeaponMapper;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Objects;

import static com.danielvm.destiny2bot.enums.EntityTypeEnum.ITEM_INVENTORY_DEFINITION;
import static com.danielvm.destiny2bot.enums.EntityTypeEnum.STAT_DEFINITION;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterWeaponsService {

    private static final String IMAGE_URL_ROOT = "http://www.bungie.net";

    private final BungieProfileClient bungieProfileClient;
    private final MembershipService membershipService;
    private final BungieManifestClientWrapper bungieManifestClient;
    private final VaultWeaponMapper vaultWeaponMapper;

    /**
     * Get all the weapons in the vault for the current user asynchronously
     *
     * @param bearerToken The user's bearer token
     * @return {@link CharacterWeaponsResponse}
     */
    public Mono<CharacterVault> getVaultWeaponsRx(String bearerToken) {
        return membershipService.getCurrentUserMembershipInformationRx(bearerToken)
                .flatMap(membershipResponse -> {
                    var membershipId = MembershipUtil.extractMembershipId(membershipResponse);
                    var membershipType = MembershipUtil.extractMembershipType(membershipResponse);
                    return bungieProfileClient.getCharacterVaultItemsRx(bearerToken, membershipType, membershipId);
                })
                .flatMapMany(items ->
                        Flux.fromIterable(items.getResponse().getProfileInventory().getData().getItems()))
                .flatMap(this::mapToCharacterWeapon
                )
                .collectList()
                .map(list -> CharacterVault.builder()
                        .weapons(list).build());
    }

    private Mono<CharacterWeapon> mapToCharacterWeapon(VaultItem item) {
        CharacterWeapon weapon = new CharacterWeapon();
        return bungieManifestClient.getManifestEntityRx(
                        ITEM_INVENTORY_DEFINITION.getId(), item.getItemHash())
                .flatMap(entity -> {
                    var response = entity.getResponse();
                    var isWeapon = Objects.equals(ItemTypeEnum.findByCode(response.getItemType()),
                            ItemTypeEnum.WEAPON);
                    if (isWeapon) {
                        weapon.setWeaponType(ItemSubTypeEnum.findById(response.getItemSubType()));
                        weapon.setWeaponName(response.getDisplayProperties().getName());
                        weapon.setWeaponIcon(response.getDisplayProperties().getHasIcon() ?
                                IMAGE_URL_ROOT + response.getDisplayProperties().getIcon() : null);
                    } else {
                        return Mono.empty();
                    }
                    return Flux.fromIterable(entity.getResponse().getStats().getStats().entrySet())
                            .flatMap(entry -> bungieManifestClient.getManifestEntityRx(
                                            STAT_DEFINITION.getId(), entry.getKey())
                                    .map(obj -> Tuples.of(entry, obj)))
                            .map(statManifest -> new Stats(statManifest.getT2().getResponse().getDisplayProperties().getName(),
                                    statManifest.getT1().getValue().getValue()))
                            .collectList()
                            .map(stats -> {
                                weapon.setStats(stats);
                                return weapon;
                            });
                });
    }

    /**
     * Get all the weapons in the vault for the current user asynchronously
     *
     * @param bearerToken The user's bearer token
     * @return {@link CharacterWeaponsResponse}
     */
    public CharacterVault getVaultWeapons(String bearerToken) {
        var membershipInfo = membershipService.getCurrentUserMembershipInformation(bearerToken);
        var membershipType = MembershipUtil.extractMembershipType(membershipInfo);
        var membershipId = MembershipUtil.extractMembershipId(membershipInfo);

        var vaultWeapons = bungieProfileClient.getCharacterVaultItems(
                bearerToken, membershipType, membershipId);

        return CharacterVault.builder()
                .weapons(Objects.requireNonNull(vaultWeapons.getBody()).getResponse()
                        .getProfileInventory().getData().getItems().stream()
                        .map(vaultWeaponMapper::entityToWeapon)
                        .toList())
                .build();
    }
}
