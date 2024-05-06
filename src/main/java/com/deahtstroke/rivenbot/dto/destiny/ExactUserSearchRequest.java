package com.deahtstroke.rivenbot.dto.destiny;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExactUserSearchRequest {

  private String displayName;

  private Integer displayNameCode;
}
