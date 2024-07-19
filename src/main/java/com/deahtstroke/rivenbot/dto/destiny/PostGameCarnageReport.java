package com.deahtstroke.rivenbot.dto.destiny;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostGameCarnageReport {

  /**
   * Fall back Post Game Carnage Report for empty responses or for big responses (> 16KB)
   */
  public static final PostGameCarnageReport EMPTY_RESPONSE = new PostGameCarnageReport(
      Instant.now(Clock.systemUTC()), false, Collections.emptyList());

  private Instant period;

  private Boolean activityWasStartedFromBeginning;

  private List<PGCREntry> entries;
}
