package com.deahtstroke.rivenbot.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmbeddedAuthor {

  /**
   * The author's name
   */
  private String name;

  /**
   * The author's url (only supports Https)
   */
  private String url;

  /**
   * url of author icon (only supports https & attachments)
   */
  @JsonProperty("icon_url")
  private String iconUrl;

  /**
   * proxied url of author icon (only supports https & attachments)
   */
  @JsonProperty
  private String iconProxyUrl;
}
