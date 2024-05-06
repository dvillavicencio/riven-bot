package com.deahtstroke.rivenbot.dto.destiny;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Most of the responses from Bungie.net have a Json element named 'Response' with arbitrary info
 * depending on the endpoint. This field is just a generic-wrapper for it. It also includes some
 * pretty generic info regarding Bungie's APIs.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BungieResponse<T> {

  @JsonAlias("Response")
  @Nullable
  private T response;
}
