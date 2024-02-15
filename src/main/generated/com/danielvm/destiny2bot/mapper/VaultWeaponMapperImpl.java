package com.danielvm.destiny2bot.mapper;

import com.danielvm.destiny2bot.dto.CharacterWeapon;
import com.danielvm.destiny2bot.dto.destiny.character.vaultitems.VaultItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-11-21T12:21:57-0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.1 (Oracle Corporation)"
)
@Component
public class VaultWeaponMapperImpl extends VaultWeaponMapper {

    @Override
    public CharacterWeapon entityToWeapon(VaultItem item) {
        if ( item == null ) {
            return null;
        }

        CharacterWeapon characterWeapon = new CharacterWeapon();

        characterWeapon.setItemInstanceId( item.getItemInstanceId() );

        toCharacterWeapon( item, characterWeapon );

        return characterWeapon;
    }
}
