package com.deahtstroke.rivenbot.dto.discord;

import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InteractionResponse implements Serializable {

  /**
   * The type of the InteractionResponse
   * <br>
   * {@link InteractionResponseType}
   */
  private Integer type;
  /**
   * Data attached to the Interaction Response
   */
  private InteractionResponseData data;

  /**
   * Creates the default response for a PING request
   *
   * @return InteractionResponse with null data and type 1
   */
  public static InteractionResponse pingResponse() {
    return new InteractionResponse(1, null);
  }
}
