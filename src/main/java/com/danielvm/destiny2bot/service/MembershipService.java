package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieMembershipClient;
import com.danielvm.destiny2bot.dto.destiny.membership.MembershipResponse;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@Slf4j
public class MembershipService {

    private static final String BEARER_TOKEN_FORMAT = "Bearer %s";
    private final BungieMembershipClient membershipClient;

    public MembershipService(BungieMembershipClient membershipClient) {
        this.membershipClient = membershipClient;
    }

    /**
     * Get the current membership information for the currently logged-in user
     *
     * @return {@link MembershipResponse}
     */
    public Mono<MembershipResponse> getCurrentUserMembershipInformation(Authentication authentication) throws Exception {
        String accessToken = ((OAuth2AuthenticationToken) authentication)
                .getPrincipal().getAttribute("access_token");
        return membershipClient.getMembershipForCurrentUser(
                        BEARER_TOKEN_FORMAT.formatted(accessToken))
                .map(data -> {
                    Assert.notNull(data, "The membership characters for the current user is null");
                    Assert.notNull(MembershipUtil.extractMembershipId(data), "Membership Id is null for current user");
                    Assert.notNull(MembershipUtil.extractMembershipId(data), "Membership Type is null for current user");
                    return data;
                });
    }


}
