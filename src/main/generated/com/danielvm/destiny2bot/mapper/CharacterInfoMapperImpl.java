package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.client.BungieManifestClient;
import com.danielvm.destiny2bot.dto.CharacterDetail;
import com.danielvm.destiny2bot.dto.destiny.character.info.CharacterInfo;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-11-21T12:21:57-0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class CharacterInfoMapperImpl implements CharacterInfoMapper {

    @Override
    public CharacterDetail toResponse(CharacterInfo data, BungieManifestClient bungieManifestClient) {
        if ( data == null ) {
            return null;
        }

        CharacterDetail characterDetail = new CharacterDetail();

        if ( data.getClassHash() != null ) {
            characterDetail.setClassName( classHash( String.valueOf( data.getClassHash() ), bungieManifestClient ) );
        }
        characterDetail.setStats( statsHash( data.getStats(), bungieManifestClient ) );

        return characterDetail;
    }
}
