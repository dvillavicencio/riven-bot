package com.deahtstroke.rivenbot.dto.destiny;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExactUserSearchRequest {

  private String displayName;

  private String displayNameCode;
}
