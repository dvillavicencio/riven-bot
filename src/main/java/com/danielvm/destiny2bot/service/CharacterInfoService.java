package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieCharacterClient;
import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.dto.CharactersResponse;
import com.danielvm.destiny2bot.mapper.CharacterMapper;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CharacterInfoService {

    private final BungieCharacterClient bungieCharacterClient;
    private final BungieManifestClient bungieManifestClient;
    private final MembershipService membershipService;
    private final CharacterMapper characterMapper;

    public CharacterInfoService(
            BungieCharacterClient bungieCharacterClient,
            BungieManifestClient bungieManifestClient,
            MembershipService membershipService,
            CharacterMapper characterMapper) {
        this.bungieCharacterClient = bungieCharacterClient;
        this.bungieManifestClient = bungieManifestClient;
        this.membershipService = membershipService;
        this.characterMapper = characterMapper;
    }

    /**
     * Get character info for ALL characters for current user
     *
     * @param authentication The authenticated user's details (including Access_token from Bungie)
     * @return {@link Mono} of {@link CharactersResponse}
     * @throws Exception an Exception
     */
    public Mono<CharactersResponse> getCharacterInfoForCurrentUser(Authentication authentication) throws Exception {
        return membershipService.getCurrentUserMembershipInformation(authentication)
                .map(data -> {
                    var bearerToken = AuthenticationUtil.getBearerToken(authentication);
                    var membershipId = MembershipUtil.extractMembershipId(data);
                    var membershipType = MembershipUtil.extractMembershipType(data);
                    return bungieCharacterClient.getCharacterDetails(bearerToken, membershipId, membershipType);
                })
                .flatMap(data -> data.map(c -> new CharactersResponse(c.response().characters().data().values().stream()
                        .map(info -> characterMapper.mapToDto(info, bungieManifestClient))
                        .toList())));
    }
}

