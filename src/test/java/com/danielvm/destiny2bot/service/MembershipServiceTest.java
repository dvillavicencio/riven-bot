package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.client.BungieMembershipClient;
import com.danielvm.destiny2bot.dto.destiny.membership.DestinyMembershipData;
import com.danielvm.destiny2bot.dto.destiny.membership.MembershipResponse;
import com.danielvm.destiny2bot.dto.destiny.membership.Memberships;
import com.danielvm.destiny2bot.util.AuthenticationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MembershipServiceTest {

    @Mock
    private BungieMembershipClient bungieClientMock;

    @InjectMocks
    private MembershipService sut;

    @Test
    @DisplayName("Get membership for user should work as expected")
    public void testGetCurrentMembershipInfo() {
        // given
        var bearerToken = "SomeBearerToken";
        var membershipResponse = new MembershipResponse(
                new Memberships(List.of(new DestinyMembershipData(3, "membershipId"))));
        var mockAuthentication = createMockAuthentication(bearerToken);

        when(bungieClientMock.getMembershipForCurrentUser(AuthenticationUtil.getBearerToken(mockAuthentication)))
                .thenReturn(ResponseEntity.ok(membershipResponse));

        // when
        var response = sut.getCurrentUserMembershipInformation(mockAuthentication);

        // then
        assertAll("Destiny Membership response is correct",
                () -> assertEquals(response.response().destinyMemberships().get(0).membershipId(),
                        response.response().destinyMemberships().get(0).membershipId()),
                () -> assertEquals(response.response().destinyMemberships().get(0).membershipType(),
                        response.response().destinyMemberships().get(0).membershipType()));
    }

    @Test
    @DisplayName("Get membership for user should fail if membership response data is null")
    public void getCurrentMembershipNullData() {
        // given
        var bearerToken = "SomeBearerToken";
        var mockAuthentication = createMockAuthentication(bearerToken);

        when(bungieClientMock.getMembershipForCurrentUser(AuthenticationUtil.getBearerToken(mockAuthentication)))
                .thenReturn(ResponseEntity.ok(null));

        // when
        assertThrows(IllegalArgumentException.class,
                () -> sut.getCurrentUserMembershipInformation(mockAuthentication),
                "The membership characters for the current user is null");
    }

    @Test
    @DisplayName("Get membership for user should fail if membershipId is null")
    public void membershipIdNegativeTest() {
        // given
        var bearerToken = "SomeBearerToken";
        var mockAuthentication = createMockAuthentication(bearerToken);
        var membershipResponse = new MembershipResponse(
                new Memberships(List.of(new DestinyMembershipData(3, null))));
        when(bungieClientMock.getMembershipForCurrentUser(AuthenticationUtil.getBearerToken(mockAuthentication)))
                .thenReturn(ResponseEntity.ok(membershipResponse));

        // when
        assertThrows(IllegalArgumentException.class,
                () -> sut.getCurrentUserMembershipInformation(mockAuthentication),
                "Membership Id is null for current user");
    }

    @Test
    @DisplayName("Get membership for user should fail if membershipType is null")
    public void membershipTypeNegativeTest() {
        // given
        var bearerToken = "SomeBearerToken";
        var mockAuthentication = createMockAuthentication(bearerToken);
        var membershipResponse = new MembershipResponse(
                new Memberships(List.of(new DestinyMembershipData(null, "membershipId"))));

        when(bungieClientMock.getMembershipForCurrentUser(AuthenticationUtil.getBearerToken(mockAuthentication)))
                .thenReturn(ResponseEntity.ok(membershipResponse));

        // when
        assertThrows(IllegalArgumentException.class,
                () -> sut.getCurrentUserMembershipInformation(mockAuthentication),
                "Membership Type is null for current user");
    }

    private OAuth2AuthenticationToken createMockAuthentication(String bearerToken) {
        return new OAuth2AuthenticationToken(
                new DefaultOAuth2User(Collections.emptyList(),
                        Map.of("access_token", bearerToken, "name", "no_name"), "name"),
                Collections.emptyList(), "bungieOAuth2Client");
    }
}
