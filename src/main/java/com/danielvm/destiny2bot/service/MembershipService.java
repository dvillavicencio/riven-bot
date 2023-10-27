package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.MembershipClient;
import com.danielvm.destiny2bot.dto.destiny.membership.DestinyMembershipResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class MembershipService {

    private final MembershipClient membershipClient;

    public MembershipService(MembershipClient membershipClient) {
        this.membershipClient = membershipClient;
    }

    /**
     * Get the current membership information for the currently logged-in user
     *
     * @return {@link DestinyMembershipResponse}
     */
    public DestinyMembershipResponse getCurrentUserMembershipInformation() throws Exception {
        var membershipData = membershipClient.getMembershipDataForCurrentUser();
        var membershipId = extractMembershipId(membershipData);
        var membershipType = membershipData.getResponse().getDestinyMemberships().get(0).getMembershipType();
        if (Objects.isNull(membershipId) || Objects.isNull(membershipType)) {
            log.error("Some required parameters are null: membershipId [{}], membershipType [{}]",
                    membershipId, membershipType);
            throw new Exception("Something unexpected happened, check the logs");
        }
        return membershipData;
    }

    /**
     * Utility to extract the membershipId
     *
     * @param response the membership response from Bungie
     * @return the membershipId
     */
    public static String extractMembershipId(DestinyMembershipResponse response) {
        return response.getResponse().getDestinyMemberships().get(0).getMembershipId();
    }

    /**
     * Utility to extract the membershipType
     *
     * @param response the membership response from Bungie
     * @return the membershipType
     */
    public static Integer extractMembershipType(DestinyMembershipResponse response) {
        return response.getResponse().getDestinyMemberships().get(0).getMembershipType();
    }
}
