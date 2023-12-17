package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieClient;
import com.danielvm.destiny2bot.dto.destiny.membership.MembershipResponse;
import com.danielvm.destiny2bot.util.MembershipUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service
@Slf4j
public class MembershipService {

  private final BungieClient bungieClient;

  public MembershipService(BungieClient bungieClient) {
    this.bungieClient = bungieClient;
  }

  /**
   * Get the current membership information for the currently logged-in user
   *
   * @param bearerToken The user's bearer token
   * @return {@link MembershipResponse}
   */
  public MembershipResponse getCurrentUserMembershipInformation(String bearerToken) {
    var membershipData = bungieClient.getMembershipForCurrentUser(
        bearerToken).getBody();

    Assert.notNull(membershipData, "The membership characters for the current user is null");
    Assert.notNull(MembershipUtil.extractMembershipId(membershipData),
        "Membership Id is null for current user");
    Assert.notNull(MembershipUtil.extractMembershipType(membershipData),
        "Membership Type is null for current user");
    return membershipData;
  }

}
