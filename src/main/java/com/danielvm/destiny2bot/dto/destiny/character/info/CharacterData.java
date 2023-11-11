package com.danielvm.destiny2bot.dto.destiny.character.info;

import com.danielvm.destiny2bot.dto.destiny.DataResponse;
import lombok.Data;

import java.util.Map;

@Data
public class CharacterData implements DataResponse {

    /**
     * Data grouped by characterId
     */
    private Map<String, CharacterInfo> data;
}
