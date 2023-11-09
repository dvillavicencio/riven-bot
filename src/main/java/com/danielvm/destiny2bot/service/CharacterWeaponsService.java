package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.client.BungieProfileClient;
import com.danielvm.destiny2bot.dto.CharacterVault;
import com.danielvm.destiny2bot.dto.CharacterWeaponsResponse;
import com.danielvm.destiny2bot.dto.destiny.character.vaultitems.VaultItem;
import com.danielvm.destiny2bot.enums.EntityTypeEnum;
import com.danielvm.destiny2bot.enums.ItemTypeEnum;
import com.danielvm.destiny2bot.mapper.CharacterWeaponMapper;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CharacterWeaponsService {

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

}
