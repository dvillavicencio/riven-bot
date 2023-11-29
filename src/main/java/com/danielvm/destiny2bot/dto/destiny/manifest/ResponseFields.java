package com.danielvm.destiny2bot.dto.destiny.manifest;

import lombok.Data;

@Data
public class ResponseFields {

  private DisplayProperties displayProperties;

  private Stats stats;

  private EquipmentBlock equipmentBlock;

  private String defaultDamageType;

  private Integer itemType;

  private Integer itemSubType;
}
