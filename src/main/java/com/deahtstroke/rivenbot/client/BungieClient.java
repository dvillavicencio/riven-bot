package com.deahtstroke.rivenbot.client;

import com.deahtstroke.rivenbot.dto.destiny.ActivitiesResponse;
import com.deahtstroke.rivenbot.dto.destiny.BungieResponse;
import com.deahtstroke.rivenbot.dto.destiny.ExactUserSearchRequest;
import com.deahtstroke.rivenbot.dto.destiny.ExactUserSearchResponse;
import com.deahtstroke.rivenbot.dto.destiny.MemberGroupResponse;
import com.deahtstroke.rivenbot.dto.destiny.MembershipResponse;
import com.deahtstroke.rivenbot.dto.destiny.PostGameCarnageReport;
import com.deahtstroke.rivenbot.dto.destiny.SearchResult;
import com.deahtstroke.rivenbot.dto.destiny.UserGlobalSearchBody;
import com.deahtstroke.rivenbot.dto.destiny.characters.CharactersResponse;
import com.deahtstroke.rivenbot.dto.destiny.manifest.ManifestResponseFields;
import com.deahtstroke.rivenbot.dto.destiny.milestone.MilestoneEntry;
import com.deahtstroke.rivenbot.enums.ManifestEntity;
import java.util.List;
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
   * @return {@link Mono} of {@link ManifestResponseFields}
   */
  @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
  Mono<BungieResponse<ManifestResponseFields>> getManifestEntity(
      @PathVariable String entityType, @PathVariable Long hashIdentifier);

  /**
   * Get public Milestones
   *
   * @return {@link Mono} of Map of {@link MilestoneEntry}
   */
  @GetExchange("/Destiny2/Milestones/")
  Mono<BungieResponse<Map<String, MilestoneEntry>>> getPublicMilestones();

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

  /**
   * Search for a Destiny 2 membership using exact parameters, that is, name and code
   *
   * @param request The request to make to Bungie for an exact user search
   * @return {@link ExactUserSearchRequest}
   */
  @PostExchange("/Destiny2/SearchDestinyPlayerByBungieName/-1/")
  Mono<BungieResponse<List<ExactUserSearchResponse>>> searchUserByExactNameAndCode(
      @RequestBody ExactUserSearchRequest request
  );
}
