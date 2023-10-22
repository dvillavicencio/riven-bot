package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieApiClient;
import com.danielvm.destiny2bot.dto.destinydomain.profile.DestinyProfileResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

@Service
public class DestinyCharacterService {

    private final BungieApiClient bungieClient;

    public DestinyCharacterService(BungieApiClient bungieApiClient) {
        this.bungieClient = bungieApiClient;
    }

    /**
     * Get details for all the item that belong to all characters of a Destiny 2 user
     *
     * @return
     * @throws Exception
     */
    public DestinyProfileResponse getAllItems() throws Exception {
        var membershipData = bungieClient.getMembershipDataForCurrentUser();
        var membershipId = CollectionUtils.isEmpty(membershipData.getResponse().getDestinyMemberships()) ?
                null : membershipData.getResponse().getDestinyMemberships().get(0).getMembershipId();
        var membershipType = CollectionUtils.isEmpty(membershipData.getResponse().getDestinyMemberships()) ?
                null : membershipData.getResponse().getDestinyMemberships().get(0).getMembershipType();
        return bungieClient.getItemDataForCurrentUser(membershipId, membershipType);
    }
}
