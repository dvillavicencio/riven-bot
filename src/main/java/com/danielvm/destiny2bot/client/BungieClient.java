package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.ActivitiesResponse;
import com.danielvm.destiny2bot.dto.destiny.BungieResponse;
import com.danielvm.destiny2bot.dto.destiny.MemberGroupResponse;
import com.danielvm.destiny2bot.dto.destiny.MembershipResponse;
import com.danielvm.destiny2bot.dto.destiny.PostGameCarnageReport;
import com.danielvm.destiny2bot.dto.destiny.SearchResult;
import com.danielvm.destiny2bot.dto.destiny.UserGlobalSearchBody;
import com.danielvm.destiny2bot.dto.destiny.characters.CharactersResponse;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import com.danielvm.destiny2bot.dto.destiny.milestone.MilestoneEntry;
import com.danielvm.destiny2bot.enums.ManifestEntity;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.PostExchange;
import reactor.core.publisher.Mono;

/**
 * This client is responsible for making calls to the Bungie API
 */
public interface BungieClient {

  /**
   * Gets the membership info for a user using their membershipId and their membershipType
   *
   * @param membershipType The membership type of the user
   * @param membershipId   The membershipId of the user
   * @return {@link MembershipResponse}
   */
  @GetExchange("/User/GetMembershipsById/{membershipId}/{membershipType}/")
  Mono<BungieResponse<MembershipResponse>> getMembershipInfoById(
      @PathVariable String membershipId, @PathVariable Integer membershipType);

  /**
   * Ges a manifest entity from the Manifest API asynchronously
   *
   * @param entityType     The entity type (see {@link ManifestEntity})
   * @param hashIdentifier The entity hash identifier
   * @return {@link Mono} of {@link ResponseFields}
   */
  @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
  Mono<BungieResponse<ResponseFields>> getManifestEntity(
      @PathVariable String entityType, @PathVariable Long hashIdentifier);

  /**
   * Get public Milestones
   *
   * @return {@link Mono} of Map of {@link MilestoneEntry}
   */
  @GetExchange("/Destiny2/Milestones/")
  Mono<BungieResponse<Map<String, MilestoneEntry>>> getPublicMilestonesRx();

  /**
   * Get a user characters
   *
   * @param membershipType      the membership type of the user
   * @param destinyMembershipId the destiny membership id of the user
   * @return {@link Mono} containing {@link CharactersResponse}
   */
  @GetExchange("/Destiny2/{membershipType}/Profile/{destinyMembershipId}/?components=200")
  Mono<BungieResponse<CharactersResponse>> getUserCharacters(
      @PathVariable Integer membershipType,
      @PathVariable String destinyMembershipId
  );

  /**
   * Search for Bungie users using a name prefix and get specific pages based on search results
   *
   * @param searchBody the search body for the request
   * @param page       the page number requested
   * @return the paged-list of the responses
   */
  @PostExchange("/User/Search/GlobalName/{page}/")
  Mono<BungieResponse<SearchResult>> searchByGlobalName(
      @RequestBody UserGlobalSearchBody searchBody,
      @PathVariable Integer page);

  /**
   * Get the groups for a specific user based on their membershipId and their membershipType. This
   * is mainly used to retrieve clans and groups that the user belongs to
   *
   * @param membershipType the user's membership type
   * @param membershipId   the user's membershipId
   * @param filter         specific filters that should be applied
   * @param groupType      the group type
   * @return {@link MemberGroupResponse}
   */
  @GetExchange("/GroupV2/User/{membershipType}/{membershipId}/{filter}/{groupType}/")
  Mono<BungieResponse<MemberGroupResponse>> getGroupsForMember(
      @PathVariable Integer membershipType, @PathVariable String membershipId,
      @PathVariable Integer filter, @PathVariable Integer groupType
  );

  /**
   * Get the activity history for a user per character
   *
   * @param membershipType      the user's membershipType
   * @param destinyMembershipId the user's membershipId
   * @param characterId         the ID of the character
   * @param count               the count of entries that should be returned (max.250)
   * @param mode                the mode of the activity to get
   * @param page                the number of the page to get
   * @return {@link ActivitiesResponse}
   */
  @GetExchange("/Destiny2/{membershipType}/Account/{destinyMembershipId}/Character/{characterId}/Stats/Activities/")
  Mono<BungieResponse<ActivitiesResponse>> getActivityHistory(@PathVariable Integer membershipType,
      @PathVariable String destinyMembershipId, @PathVariable String characterId,
      @RequestParam Integer count, @RequestParam Integer mode, @RequestParam Integer page);

  /**
   * Get a Post Game Carnage Report (PGCR)
   *
   * @param activityId the ID of the activity to get
   * @return {@link PostGameCarnageReport}
   */
  @GetExchange("/Destiny2/Stats/PostGameCarnageReport/{activityId}/")
  Mono<BungieResponse<PostGameCarnageReport>> getPostGameCarnageReport(
      @PathVariable Long activityId
  );
}
