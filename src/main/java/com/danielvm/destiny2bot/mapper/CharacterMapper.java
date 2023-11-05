package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.dto.CharacterDetail;
import com.danielvm.destiny2bot.dto.Stats;
import com.danielvm.destiny2bot.dto.destiny.character.info.CharacterInfo;
import com.danielvm.destiny2bot.enums.EntityTypeEnum;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "Spring")
public interface CharacterMapper {

    @Mapping(target = "stats", source = "stats", qualifiedByName = "statsDefinition")
    @Mapping(target = "className", source = "infoDto.classHash", qualifiedByName = "classDefinition")
    CharacterDetail mapToDto(CharacterInfo infoDto, @Context BungieManifestClient manifestClient);

    @Named("classDefinition")
    default String getClassDefinition(Long classHash, @Context BungieManifestClient manifestClient) {
        return Objects.requireNonNull(manifestClient.getManifestEntity(EntityTypeEnum.CLASS_DEFINITION, String.valueOf(classHash))
                .block()).response().displayProperties().name();
    }

    @Named("statsDefinition")
    default List<Stats> statsDefinition(Map<String, Integer> stats, @Context BungieManifestClient manifestClient) {
        return stats.entrySet().stream()
                .map(entry -> {
                    var statName = Objects.requireNonNull(manifestClient
                                    .getManifestEntity(EntityTypeEnum.STAT_DEFINITION, entry.getKey()).block())
                            .response().displayProperties().name();
                    return new Stats(statName, entry.getValue());
                }).toList();
    }

}
