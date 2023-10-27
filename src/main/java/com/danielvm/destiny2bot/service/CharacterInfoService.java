package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.CharacterClient;
import com.danielvm.destiny2bot.client.ManifestClient;
import com.danielvm.destiny2bot.dto.CharacterDetailsResponse;
import com.danielvm.destiny2bot.dto.destiny.profile.CharacterInfoResponse;
import com.danielvm.destiny2bot.mapper.CharacterMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CharacterInfoService {

    private final CharacterClient characterClient;
    private final ManifestClient manifestClient;
    private final MembershipService membershipService;
    private final CharacterMapper characterMapper;

    public CharacterInfoService(
            CharacterClient characterClient,
            ManifestClient manifestClient, MembershipService membershipService,
            CharacterMapper characterMapper) {
        this.characterClient = characterClient;
        this.manifestClient = manifestClient;
        this.membershipService = membershipService;
        this.characterMapper = characterMapper;
    }

    /**
     * Get character info for ALL characters for current user
     *
     * @return {@link CharacterInfoResponse}
     * @throws Exception an Exception
     */
    public CharacterDetailsResponse getCharacterInfoForCurrentUser() throws Exception {
        var membershipData = membershipService.getCurrentUserMembershipInformation();

        var membershipId = MembershipService.extractMembershipId(membershipData);
        var membershipType = MembershipService.extractMembershipType(membershipData);

        return new CharacterDetailsResponse(characterClient.getDetailsPerCharacter(membershipId, membershipType)
                .getResponse().getCharacters().getData().values().stream()
                .map(characterInfoDto -> characterMapper.mapDestinyDtoToResponseDto(characterInfoDto, manifestClient))
                .toList());
    }
}

