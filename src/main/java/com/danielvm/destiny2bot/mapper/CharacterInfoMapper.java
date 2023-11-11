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
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface CharacterInfoMapper {

    @Mapping(target = "className", source = "data.classHash", qualifiedByName = "classHash")
    @Mapping(target = "stats", source = "data.stats", qualifiedByName = "statsHash")
    CharacterDetail toResponse(CharacterInfo data, @Context BungieManifestClient bungieManifestClient);

    @Named("statsHash")
    default List<Stats> statsHash(Map<String, Integer> statsMap, @Context BungieManifestClient client) {
        return statsMap.entrySet().stream()
                .map(entry -> {
                    var entity = client.getManifestEntity(EntityTypeEnum.STAT_DEFINITION.getId(),
                            String.valueOf(entry.getKey())).getBody();
                    Assert.notNull(entity, "Entity received for stat hash [%s] is null".formatted(entry.getValue()));
                    return new Stats(entity.getResponse().getDisplayProperties().getName(), entry.getValue());
                }).toList();
    }

    @Named("classHash")
    default String classHash(String classHash, @Context BungieManifestClient client) {
        var entity = client.getManifestEntity(EntityTypeEnum.CLASS_DEFINITION.getId(), classHash).getBody();
        Assert.notNull(entity, "Entity received for class hash [%s] is null".formatted(classHash));
        return entity.getResponse().getDisplayProperties().getName();
    }
}
