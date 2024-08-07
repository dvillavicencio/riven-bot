package com.deahtstroke.rivenbot.dto.discord;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Embedded {

  private String title;

  private String type;

  private String description;

  private String url;

  private String timestamp;

  private Integer color;

  private EmbeddedFooter footer;

  private EmbeddedImage image;

  private EmbeddedThumbnail thumbnail;

  private Object video;

  private EmbeddedProvider provider;

  private EmbeddedAuthor author;

  private List<EmbeddedField> fields;
}
