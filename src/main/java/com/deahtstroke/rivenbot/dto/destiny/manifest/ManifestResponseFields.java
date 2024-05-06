package com.deahtstroke.rivenbot.dto.destiny.manifest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManifestResponseFields {

  private DisplayProperties displayProperties;

  private Stats stats;

  private EquipmentBlock equipmentBlock;

  private String defaultDamageType;

  private Integer itemType;

  private Integer itemSubType;

  private Integer directActivityModeType;

  private Long activityTypeHash;

  private Long hash;
}
