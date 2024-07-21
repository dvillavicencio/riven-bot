package com.deahtstroke.rivenbot.dto.discord;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InteractionData {

  /**
   * The Id of the invoked command
   */
  private Object id;

  /**
   * The name of the invoked command
   */
  private String name;

  /**
   * The type of the invoked command
   */
  private Integer type;

  /**
   * List of options sent with the interaction
   */
  private List<Option> options;

  /**
   * Custom Id of a message component
   */
  @JsonProperty("custom_id")
  private String customId;

  /**
   * The type of the component that was sent
   */
  @JsonProperty("component_type")
  private Integer componentType;

  /**
   * List of resolved values for a select menu option by a user
   */
  private List<String> values;
}
