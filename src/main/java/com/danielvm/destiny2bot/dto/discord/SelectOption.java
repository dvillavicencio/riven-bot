package com.danielvm.destiny2bot.dto.discord;

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
public class SelectOption {

  /**
   * Label for the option in a select-menu, user-facing name of the option, max 100 characters
   */
  private String label;

  /**
   * Dev-defined value of the option, max 100 characters
   */
  private String value;

  /**
   * Additional description of the option, max 100 characters
   */
  private String description;

  /**
   * Emoji for this select option
   */
  private Emoji emoji;

  /**
   * Whether this option is the default one for the select menu
   */
  @JsonProperty("default")
  private Boolean def;
}
