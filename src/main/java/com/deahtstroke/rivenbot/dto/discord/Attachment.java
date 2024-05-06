package com.deahtstroke.rivenbot.dto.discord;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class Attachment {

  /**
   * Attachment Id
   */
  private Object id;

  /**
   * Name of the file
   */
  private String filename;

  /**
   * Description of the file
   */
  private String description;

  /**
   * Content type of the file
   */
  @JsonProperty("content_type")
  private String contentType;

  /**
   * Size of the file
   */
  private Integer size;

  /**
   * url of the source file
   */
  private String url;

  /**
   * Proxy URL of the file
   */
  @JsonProperty("proxy_url")
  private String proxyUrl;

  /**
   * Height of the file (if its an image)
   */
  private Integer height;

  /**
   * Width of the image (if its an image)
   */
  private Integer width;

  /**
   * Whether this message is ephemeral (it expires)
   */
  private Boolean ephemeral;

  /**
   * Duration in seconds if the file is an audio
   */
  private Double durationSecs;

  /**
   * Base64 encoded bytearray representing a sampled waveform (for voice messages)
   */
  private String waveform;

  /**
   * Attachment flags combined as a bitfield
   */
  private Integer flags;
}
