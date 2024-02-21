package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.dto.destiny.PGCREntry;
import com.danielvm.destiny2bot.dto.destiny.PostGameCarnageReport;
import com.danielvm.destiny2bot.entity.PGCRDetails;
import com.danielvm.destiny2bot.entity.PlayerEntryDetails;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PGCRMapper {

  @Mapping(target = "fromBeginning", source = "report.activityWasStartedFromBeginning")
  @Mapping(target = "players", source = "report.entries")
  PGCRDetails dtoToEntity(PostGameCarnageReport report, Long instanceId);

  List<PlayerEntryDetails> dtosToEntities(List<PGCREntry> entries);

  @Mapping(target = "playerName", source = "entry.player.destinyUserInfo.bungieGlobalDisplayName")
  @Mapping(target = "playerTag", source = "entry.player.destinyUserInfo.bungieGlobalDisplayNameCode")
  @Mapping(target = "playerIcon", source = "entry.player.destinyUserInfo.iconPath")
  @Mapping(target = "membershipType", source = "entry.player.destinyUserInfo.membershipType")
  @Mapping(target = "membershipId", source = "entry.player.destinyUserInfo.membershipId")
  @Mapping(target = "deaths", expression = "java(entry.getValues().get(\"deaths\").getBasic().getValue().intValue())")
  @Mapping(target = "kills", expression = "java(entry.getValues().get(\"kills\").getBasic().getValue().intValue())")
  PlayerEntryDetails dtoToEntity(PGCREntry entry);
}
