package com.deahtstroke.rivenbot.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record EmbeddedThumbnail(String url, @JsonProperty("proxy_url") String proxyUrl,
                                Integer height, Integer width) {

}
