package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.client.BungieProfileClient;
import com.danielvm.destiny2bot.dto.CharactersResponse;
import com.danielvm.destiny2bot.mapper.CharacterInfoMapper;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CharacterInfoService {

    private final BungieProfileClient bungieProfileClient;
    private final BungieManifestClient bungieManifestClient;
    private final MembershipService membershipService;
    private final CharacterInfoMapper characterInfoMapper;

    /**
     * Get character info for ALL characters for current user
     *
     * @param bearerToken The user's bearer token
     * @return {@link Mono} of {@link CharactersResponse}
     * @throws Exception an Exception
     */
    public CharactersResponse getCharacterInfoForCurrentUser(String bearerToken) {
        var membershipInfo = membershipService.getCurrentUserMembershipInformation(bearerToken);
        Assert.notNull(membershipInfo, "Membership info for current user is null");

        var membershipId = MembershipUtil.extractMembershipId(membershipInfo);
        var membershipType = MembershipUtil.extractMembershipType(membershipInfo);

        var characterDetails = bungieProfileClient.getCharacterDetails(bearerToken, membershipType, membershipId).getBody();
        return new CharactersResponse(characterDetails.getResponse().getCharacters().getData().values().stream()
                .map(characterInfo -> characterInfoMapper.toResponse(characterInfo, bungieManifestClient))
                .toList());
    }

}

