package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieMembershipClient;
import com.danielvm.destiny2bot.dto.destiny.membership.MembershipResponse;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class MembershipService {

    private final BungieMembershipClient membershipClient;

    /**
     * Get the current membership information for the currently logged-in user
     *
     * @param authentication The object containing authentication information
     * @return {@link MembershipResponse}
     */
    public MembershipResponse getCurrentUserMembershipInformation(Authentication authentication) {
        var membershipData = membershipClient.getMembershipForCurrentUser(
                AuthenticationUtil.getBearerToken(authentication)).getBody();

        Assert.notNull(membershipData, "The membership characters for the current user is null");
        Assert.notNull(MembershipUtil.extractMembershipId(membershipData), "Membership Id is null for current user");
        Assert.notNull(MembershipUtil.extractMembershipType(membershipData), "Membership Type is null for current user");
        return membershipData;
    }

    /**
     * Get the current membership information for the currently logged-in user asynchronously
     *
     * @param authentication The object containing authentication information
     * @return {@link MembershipResponse}
     */
    public Mono<MembershipResponse> getCurrentUserMembershipInformationRx(Authentication authentication) {
        return membershipClient.getMembershipForCurrentUserRx(
                        AuthenticationUtil.getBearerToken(authentication))
                .map(membershipResponse -> {
                    Assert.notNull(membershipResponse, "The membership characters for the current user is null");
                    Assert.notNull(MembershipUtil.extractMembershipId(membershipResponse), "Membership Id is null for current user");
                    Assert.notNull(MembershipUtil.extractMembershipId(membershipResponse), "Membership Type is null for current user");
                    return membershipResponse;
                });
    }


}
