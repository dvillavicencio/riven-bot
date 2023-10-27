package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.ItemClient;
import com.danielvm.destiny2bot.dto.destiny.characters.CharacterWeaponsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WeaponsService {

    private final ItemClient itemClient;
    private final MembershipService membershipService;

    public WeaponsService(ItemClient itemClient, MembershipService membershipService) {
        this.itemClient = itemClient;
        this.membershipService = membershipService;
    }

    /**
     * Get weapons for all characters grouped by their inventory
     *
     * @return {@link CharacterWeaponsResponse}
     * @throws Exception exception (TBD which error and exception to throw)
     */
    public CharacterWeaponsResponse getAllWeapons() throws Exception {
        var membershipData = membershipService.getCurrentUserMembershipInformation();
        var membershipId = MembershipService.extractMembershipId(membershipData);
        var membershipType = MembershipService.extractMembershipType(membershipData);
        var itemData = itemClient.getItemDataForCurrentUser(membershipId, membershipType)
                .getResponse().getCharacterInventories()
                .getData().entrySet().stream()
                .map((key) -> { // key is characterId
                    return null;
                }).toList();
        return null;
    }
}
