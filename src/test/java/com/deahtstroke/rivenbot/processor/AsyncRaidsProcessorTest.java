package com.deahtstroke.rivenbot.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.deahtstroke.rivenbot.client.BungieClient;
import com.deahtstroke.rivenbot.dto.destiny.BungieNetMembership;
import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.ExactUserSearchRequest;
import com.deahtstroke.rivenbot.dto.destiny.ExactUserSearchResponse;
import com.deahtstroke.rivenbot.dto.destiny.MembershipResponse;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedField;
import com.deahtstroke.rivenbot.entity.ButtonStyle;
import com.deahtstroke.rivenbot.entity.RaidStatistics;
import com.deahtstroke.rivenbot.enums.MessageComponentId;
import com.deahtstroke.rivenbot.service.DiscordAPIService;
import com.deahtstroke.rivenbot.service.RaidStatsService;
import com.deahtstroke.rivenbot.util.MessageComponents;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AsyncRaidsProcessorTest {

  @Mock
  DiscordAPIService discordAPIService;

  @Mock
  BungieClient bungieClient;

  @Mock
  RaidStatsService raidStatsService;

  @InjectMocks
  AsyncRaidsProcessor sut;

  @Test
  @DisplayName("Processing raids async should work as expected when nothing fails")
  void shouldWorkAsExpected() {
    // given: a username, user tag and continuation token
    String username = "Deaht";
    String userTag = "5718";
    String continuationToken = "123123120319204109i32312";

    Integer membershipType = 1;
    String membershipId = "123812012012";

    String profilePicPath = "/some/profile/pic/path";

    List<ExactUserSearchResponse> userResults = List.of(
        new ExactUserSearchResponse(username, Integer.parseInt(userTag), membershipType,
            membershipId, userTag, true)
    );

    MembershipResponse membershipResponse = new MembershipResponse(null, null,
        new BungieNetMembership(membershipId, username + "#" + userTag, username, false, "en_US",
            profilePicPath));

    RaidStatistics stat1 = new RaidStatistics("Crota's End", 102, 0, 139, 5, 10, 5, 2, 8);
    RaidStatistics stat2 = new RaidStatistics("Last Wish", 102, 0, 139, 5, 10, 5, 2, 8);

    List<EmbeddedField> expectedFields = List.of(
        MessageComponents.createField(new SimpleEntry<>(stat1.getRaidName(), stat1)),
        MessageComponents.createField(new SimpleEntry<>(stat2.getRaidName(), stat2)));

    when(bungieClient.searchUserByExactNameAndCode(assertArg(ex -> {
      assertThat(ex.getDisplayName()).isEqualTo(username);
      assertThat(ex.getDisplayNameCode()).isEqualTo(userTag);
    }))).thenReturn(Mono.just(BungieResponse.of(userResults)));

    when(bungieClient.getMembershipInfoById(membershipId, membershipType))
        .thenReturn(Mono.just(BungieResponse.of(membershipResponse)));

    when(raidStatsService.calculateRaidStats(username, userTag, membershipId, membershipType))
        .thenReturn(Flux.just(stat1, stat2));

    when(discordAPIService.editOriginalInteraction(eq(continuationToken), assertArg(data -> {
      assertThat(data.getEmbeds()).hasSize(1);

      var embedFields = data.getEmbeds().get(0).getFields();
      assertThat(embedFields).containsAll(expectedFields);

      var components = data.getComponents();
      assertThat(components).hasSize(1); // only one action row

      var actionRow = components.getFirst();
      assertThat(actionRow.getType()).isEqualTo(1);

      var buttons = actionRow.getComponents();
      assertThat(buttons).hasSize(1); // only one button

      var button = buttons.getFirst();
      assertThat(button.getStyle()).isEqualTo(ButtonStyle.BLURPLE.getButtonValue());
      assertThat(button.getLabel()).isEqualTo("What is this?");
      assertThat(button.getType()).isEqualTo(2);
      assertThat(button.getCustomId()).isEqualTo(
          MessageComponentId.RAID_STATS_COMPREHENSION.getId());
    }))).thenReturn(Mono.empty());

    // when: process raids async is called
    // then: no exceptions are raised and stats are processed as normal
    StepVerifier.create(sut.processRaidsAsync(username, userTag, continuationToken))
        .verifyComplete();
  }

  @Test
  @DisplayName("Should throw an error if a user does not have any matching membership data")
  void shouldThrowErrorOnMissingMembershipData() {
    // given: a username, user tag and continuation token
    String username = "Deaht";
    String userTag = "5718";
    String continuationToken = "123123120319204109i32312";

    when(bungieClient.searchUserByExactNameAndCode(new ExactUserSearchRequest(username, userTag)))
        .thenReturn(Mono.just(BungieResponse.of(Collections.emptyList())));

    when(discordAPIService.editOriginalInteraction(eq(continuationToken), assertArg(data -> {
      var content = data.getContent();
      assertThat(content).isEqualTo(
          "User %s has no valid Destiny 2 memberships! Please contact someone from the Dev Team in order to solve this issue or try again later"
              .formatted(username + "#" + userTag));
    }))).thenReturn(Mono.empty());

    // when: process raids async is called
    // then: membership info is empty and a corresponding error message is sent through Discord chat
    StepVerifier.create(sut.processRaidsAsync(username, userTag, continuationToken))
        .verifyComplete();
  }

  @Test
  @DisplayName("Should throw an error if a user has privacy settings turned on")
  void shouldThrowErrorOnPrivacySettings() {
    // given: a username, user tag and continuation token
    String username = "Deaht";
    String userTag = "5718";
    String continuationToken = "123123120319204109i32312";

    Integer membershipType = 1;
    String membershipId = "123812012012";

    List<ExactUserSearchResponse> userResults = List.of(
        new ExactUserSearchResponse(username, Integer.parseInt(userTag), membershipType,
            membershipId, userTag, false)
    );

    when(bungieClient.searchUserByExactNameAndCode(new ExactUserSearchRequest(username, userTag)))
        .thenReturn(Mono.just(BungieResponse.of(userResults)));

    when(discordAPIService.editOriginalInteraction(eq(continuationToken), assertArg(data -> {
      var content = data.getContent();
      assertThat(content).isEqualTo(
          "Oh no! Seems that %s has their privacy settings turned on, therefore we cannot access their stuff right now. Sorry about that."
              .formatted(username + "#" + userTag));
    }))).thenReturn(Mono.empty());

    // when: process raids async is called
    // then: an error message is thrown due to the `isPublic` flag being false
    StepVerifier.create(sut.processRaidsAsync(username, userTag, continuationToken))
        .verifyComplete();
  }

  @Test
  @DisplayName("Should fail gracefully when raid statistics return an empty map")
  void shouldFailGracefullyWhenRaidStatisticsAreEmpty() {
    // given: a username, user tag and continuation token
    String username = "Deaht";
    String userTag = "5718";
    String continuationToken = "123123120319204109i32312";

    Integer membershipType = 1;
    String membershipId = "123812012012";

    String profilePicPath = "/some/profile/pic/path";

    List<ExactUserSearchResponse> userResults = List.of(
        new ExactUserSearchResponse(username, Integer.parseInt(userTag), membershipType,
            membershipId, userTag, true)
    );

    MembershipResponse membershipResponse = new MembershipResponse(null, null,
        new BungieNetMembership(membershipId, username + "#" + userTag, username, false, "en_US",
            profilePicPath));

    when(bungieClient.searchUserByExactNameAndCode(assertArg(ex -> {
      assertThat(ex.getDisplayName()).isEqualTo(username);
      assertThat(ex.getDisplayNameCode()).isEqualTo(userTag);
    }))).thenReturn(Mono.just(BungieResponse.of(userResults)));

    when(bungieClient.getMembershipInfoById(membershipId, membershipType))
        .thenReturn(Mono.just(BungieResponse.of(membershipResponse)));

    when(raidStatsService.calculateRaidStats(username, userTag, membershipId, membershipType))
        .thenReturn(Flux.empty());

    when(discordAPIService.editOriginalInteraction(eq(continuationToken), assertArg(data -> {
      assertThat(data.getContent()).isEqualTo("""
          Huh... It seems as if %s does not have any raids completed, either this or something went wrong when retrieving the data.\
          If you are sure this is a bug be sure to let one of the developers know!""".formatted(
          username + "#" + userTag));
    }))).thenReturn(Mono.empty());

    // when: process raids is called
    // then: an error message is both logged and sent to discord chat with the details
    StepVerifier.create(sut.processRaidsAsync(username, userTag, continuationToken))
        .verifyComplete();
  }

}
