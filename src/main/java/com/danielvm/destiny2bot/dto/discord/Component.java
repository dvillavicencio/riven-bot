package com.danielvm.destiny2bot.dto.discord;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Component {

  /**
   * Type of the component
   */
  private Integer type;

  /**
   * Id defined for the given component
   */
  @JsonProperty("custom_id")
  private String customId;

  /**
   * Label of the component
   */
  private String label;

  /**
   * Style used for several components, e.g., for buttons
   */
  private Integer style;

  /**
   * Url that link-style buttons use
   */
  private String url;

  /**
   * Array of options for select menu
   */
  private List<SelectOption> options;

  /**
   * Recursive list of components in the case this component manages other components
   */
  private List<Component> components;
}
