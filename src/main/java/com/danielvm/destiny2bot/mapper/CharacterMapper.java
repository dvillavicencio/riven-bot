package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.client.ManifestClient;
import com.danielvm.destiny2bot.dto.CharacterDetailDto;
import com.danielvm.destiny2bot.dto.StatDto;
import com.danielvm.destiny2bot.dto.destiny.profile.CharacterInfoDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "Spring")
public interface CharacterMapper {

    @Mapping(target = "stats", source = "stats", qualifiedByName = "statsDefinition")
    @Mapping(target = "className", source = "infoDto.classHash", qualifiedByName = "classDefinition")
    CharacterDetailDto mapDestinyDtoToResponseDto(CharacterInfoDto infoDto, @Context ManifestClient manifestClient);

    @Named("classDefinition")
    default String getClassDefinition(Long classHash, @Context ManifestClient manifestClient) {
        return manifestClient.getClassDefinition(classHash).getResponse()
                .getDisplayProperties().getName();
    }

    @Named("statsDefinition")
    default List<StatDto> statsDefinition(Map<String, Integer> stats, @Context ManifestClient manifestClient) {
        return stats.entrySet().stream()
                .map(entry -> {
                    var statName = manifestClient.getStatDefinition(entry.getKey())
                            .getResponse().getDisplayProperties().getName();
                    return StatDto.builder()
                            .statName(statName)
                            .statLevel(entry.getValue())
                            .build();
                }).toList();
    }

}
