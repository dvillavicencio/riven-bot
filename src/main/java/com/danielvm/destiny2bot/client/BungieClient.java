package com.danielvm.destiny2bot.client;

import com.danielvm.destiny2bot.dto.destiny.GenericResponse;
import com.danielvm.destiny2bot.dto.destiny.manifest.ResponseFields;
import com.danielvm.destiny2bot.dto.destiny.membership.MembershipResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;

/**
 * This client is responsible for making calls to the Bungie
 */
public interface BungieClient {

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
   * Ges a manifest entity from the Manifest API
   *
   * @param entityType     The entity type (see
   *                       {@link com.danielvm.destiny2bot.enums.EntityTypeEnum})
   * @param hashIdentifier The entity hash identifier
   * @return {@link GenericResponse} of {@link ResponseFields}
   */
  @GetExchange("/Destiny2/Manifest/{entityType}/{hashIdentifier}/")
  ResponseEntity<GenericResponse<ResponseFields>> getManifestEntity(
      @PathVariable(value = "entityType") String entityType,
      @PathVariable(value = "hashIdentifier") String hashIdentifier);

}
