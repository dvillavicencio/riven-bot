package com.deahtstroke.rivenbot.dto.discord;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddedImage {

  private String url;

  private Integer height;

  private Integer width;
}
