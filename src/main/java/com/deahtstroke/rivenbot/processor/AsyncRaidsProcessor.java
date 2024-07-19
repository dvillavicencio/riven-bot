package com.deahtstroke.rivenbot.processor;

import com.deahtstroke.rivenbot.client.BungieClient;
import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.ExactUserSearchRequest;
import com.deahtstroke.rivenbot.dto.destiny.ExactUserSearchResponse;
import com.deahtstroke.rivenbot.dto.destiny.MembershipResponse;
import com.deahtstroke.rivenbot.dto.discord.Embedded;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedAuthor;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedField;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedFooter;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedThumbnail;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponseData;
import com.deahtstroke.rivenbot.entity.ButtonStyle;
import com.deahtstroke.rivenbot.entity.RaidStatistics;
import com.deahtstroke.rivenbot.exception.BaseDiscordChatException;
import com.deahtstroke.rivenbot.exception.MembershipsNotFoundException;
import com.deahtstroke.rivenbot.exception.ProfileNotPublicException;
import com.deahtstroke.rivenbot.service.DiscordAPIService;
import com.deahtstroke.rivenbot.service.RaidStatsService;
import com.deahtstroke.rivenbot.util.MessageComponents;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AsyncRaidsProcessor {

  private static final String HASHTAG = "#";
  private static final String ICON_BASE_URL = "https://www.bungie.net/";
  private static final String STATS_TITLE = "Raid Stats for %s";
  private static final String MANATEE_ICON = "https://www.harvardreview.org/wp-content/uploads/2020/10/Manatee.jpg";
  private static final String RAID_COMPREHENSION_BUTTON_ID = "raid_stats_comprehension";
  private static final String DEFAULT_RAID_THUMBNAIL_URL = "https://d1lss44hh2trtw.cloudfront.net/resize?type=webp&url=https%3A%2F%2Fshacknews-www.s3.amazonaws.com%2Fassets%2Farticle%2F2023%2F02%2F21%2Fdestiny-2-lightfall-raid-release-time_feature.jpg&width=2064&sign=GYtYnnD6xsEp5pOb7q50FhEUvzN8cE15FT4UUpCT5HA";

  private final DiscordAPIService discordAPIService;
  private final BungieClient defaultBungieClient;
  private final RaidStatsService raidStatsService;

  public AsyncRaidsProcessor(
      DiscordAPIService discordAPIService,
      BungieClient defaultBungieClient,
      RaidStatsService raidStatsService) {
    this.discordAPIService = discordAPIService;
    this.defaultBungieClient = defaultBungieClient;
    this.raidStatsService = raidStatsService;
  }

  private static Embedded createEmbed(String displayUsername, String usernameIcon,
      List<EmbeddedField> embeddedFields) {
    var footer = EmbeddedFooter.builder()
        .iconUrl(MANATEE_ICON)
        .text("Developed by Deahtstroke")
        .build();
    var description = """
        General crunched numbers regarding all the raid clears you've done so far guardian.
        This was requested <t:%s:R>""".formatted(
        Instant.now().getEpochSecond());
    var thumbnail = EmbeddedThumbnail.builder()
        .url(DEFAULT_RAID_THUMBNAIL_URL)
        .build();
    var author = EmbeddedAuthor.builder()
        .name(displayUsername)
        .iconUrl(usernameIcon)
        .build();
    var title = STATS_TITLE.formatted(displayUsername);
    return Embedded.builder()
        .author(author)
        .title(title)
        .thumbnail(thumbnail)
        .description(description)
        .fields(embeddedFields)
        .footer(footer)
        .build();
  }

  /**
   * This method contains all the logic necessary to process a Destiny 2 user's statistics saved in
   * the DB and subsequently send it through Discord chat
   *
   * @param username          The username for which to process raids
   * @param userTag           The tag of the user to process raids for
   * @param continuationToken The token from the interaction used to update the original
   *                          interaction
   */
  public Mono<Void> processRaidsAsync(String username, String userTag, String continuationToken) {
    return createRaidStatsResponse(username, userTag)
        .onErrorResume(BaseDiscordChatException.class,
            err -> Mono.just(InteractionResponseData.builder()
                .content(err.getChatErrorMessage())
                .build()))
        .doOnError(err -> log.error(err.getMessage()))
        .flatMap(data -> discordAPIService.editOriginalInteraction(continuationToken, data));
  }

  private Mono<InteractionResponseData> createRaidStatsResponse(String username, String userTag) {
    String displayUsername = username + HASHTAG + userTag;
    return defaultBungieClient.searchUserByExactNameAndCode(
            new ExactUserSearchRequest(username, userTag))
        .filter(response -> CollectionUtils.isNotEmpty(response.getResponse()))
        .switchIfEmpty(Mono.deferContextual(ctx -> Mono.error(new MembershipsNotFoundException(
            "User [%s] does not have any valid Destiny 2 memberships".formatted(displayUsername),
            "User %s has no valid Destiny 2 memberships! Please contact someone from the Dev Team in order to solve this issue or try again later"
                .formatted(displayUsername)))))
        .flatMap(response -> {
          ExactUserSearchResponse firstResponse = response.getResponse().getFirst();
          if (Boolean.FALSE.equals(firstResponse.getIsPublic())) {
            return Mono.deferContextual(ctx -> Mono.error(new ProfileNotPublicException(
                "The Bungie.net profile for user [%s] is set to private".formatted(displayUsername),
                "Oh no! Seems that %s has their privacy settings turned on, therefore we cannot access their stuff right now. Sorry about that."
                    .formatted(displayUsername))));
          }
          String membershipId = firstResponse.getMembershipId();
          Integer membershipType = firstResponse.getMembershipType();
          return Mono.zip(
              getBungieUserInfo(membershipId, membershipType),
              createEmbedFields(username, userTag, membershipId, membershipType),
              (userInfo, fields) -> {
                String usernameIcon = ICON_BASE_URL + userInfo.getBungieNetUser()
                    .getProfilePicturePath();
                return InteractionResponseData.builder()
                    .embeds(MessageComponents.embeds(
                        createEmbed(displayUsername, usernameIcon, fields)))
                    .components(MessageComponents.builder()
                        .addActionRow(MessageComponents.actionRow()
                            .button(RAID_COMPREHENSION_BUTTON_ID, "What is this?",
                                ButtonStyle.BLURPLE))
                        .build())
                    .build();
              });
        });
  }

  private Mono<MembershipResponse> getBungieUserInfo(String membershipId, Integer membershipType) {
    return defaultBungieClient.getMembershipInfoById(membershipId, membershipType)
        .map(BungieResponse::getResponse);
  }

  private Mono<List<EmbeddedField>> createEmbedFields(String username, String userTag,
      String membershipId, Integer membershipType) {
    return raidStatsService.calculateRaidStats(username, userTag, membershipId, membershipType)
        .collectMap(RaidStatistics::getRaidName)
        .flatMapIterable(Map::entrySet)
        .map(MessageComponents::createField)
        .collectList();
  }

}
