package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieCharacterClient;
import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.dto.CharactersResponse;
import com.danielvm.destiny2bot.mapper.CharacterInfoMapper;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class CharacterInfoService {

    private final BungieCharacterClient bungieCharacterClient;
    private final BungieManifestClient bungieManifestClient;
    private final MembershipService membershipService;
    private final CharacterInfoMapper characterInfoMapper;

    /**
     * Get character info for ALL characters for current user
     *
     * @param authentication The authenticated user's details (including Access_token from Bungie)
     * @return {@link Mono} of {@link CharactersResponse}
     * @throws Exception an Exception
     */
    public CharactersResponse getCharacterInfoForCurrentUser(Authentication authentication) throws Exception {
        var membershipInfo = membershipService.getCurrentUserMembershipInformation(authentication);
        Assert.notNull(membershipInfo, "Membership info for current user is null");

        var bearerToken = AuthenticationUtil.getBearerToken(authentication);
        var membershipId = MembershipUtil.extractMembershipId(membershipInfo);
        var membershipType = MembershipUtil.extractMembershipType(membershipInfo);

        var characterDetails = bungieCharacterClient.getCharacterDetails(bearerToken, membershipId, membershipType).getBody();
        return new CharactersResponse(characterDetails.response().characters().data().values().stream()
                .map(characterInfo -> characterInfoMapper.toResponse(characterInfo, bungieManifestClient))
                .toList());
    }
}

