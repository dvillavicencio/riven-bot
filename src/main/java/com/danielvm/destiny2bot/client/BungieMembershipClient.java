package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.membership.MembershipResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

public interface BungieMembershipClient {

    @GetExchange("/User/GetMembershipsForCurrentUser/")
    ResponseEntity<MembershipResponse> getMembershipForCurrentUser(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION) String bearerToken);
}
