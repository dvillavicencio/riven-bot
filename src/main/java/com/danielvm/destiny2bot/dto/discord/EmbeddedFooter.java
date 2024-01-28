package com.danielvm.destiny2bot.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EmbeddedFooter {

  /**
   * Footer text
   */
  private String text;

  /**
   * url of the footer icon
   */
  @JsonProperty("icon_url")
  private String iconUrl;

  /**
   * proxied url of the icon
   */
  @JsonProperty("proxy_icon_url")
  private String proxyIconUrl;
}
