package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.membership.MembershipResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

public interface BungieMembershipClient {

    /**
     * Gets the membership info for the current user
     *
     * @param bearerToken The user's bearer token
     * @return {@link MembershipResponse}
     */
    @GetExchange("/User/GetMembershipsForCurrentUser/")
    ResponseEntity<MembershipResponse> getMembershipForCurrentUser(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken);

    /**
     * Gets the membership info for the current user asynchronously
     *
     * @param bearerToken The user's bearer token
     * @return {@link MembershipResponse}
     */
    @GetExchange("/User/GetMembershipsForCurrentUser/")
    Mono<MembershipResponse> getMembershipForCurrentUserRx(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken);
}
