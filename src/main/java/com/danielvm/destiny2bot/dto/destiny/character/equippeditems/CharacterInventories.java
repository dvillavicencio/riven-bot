package com.danielvm.destiny2bot.dto.destiny.character.equippeditems;

import com.danielvm.destiny2bot.dto.destiny.DataResponse;
import lombok.Data;

import java.util.Map;

@Data
public class CharacterInventories implements DataResponse {
    private Map<String, ItemsList> data;
}