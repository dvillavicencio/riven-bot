package com.danielvm.destiny2bot.dto.destiny;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class MembershipResponse {

  private List<DestinyMembershipData> destinyMemberships;

  private String primaryMembershipId;

  private BungieNetMembership bungieNetUser;
}
