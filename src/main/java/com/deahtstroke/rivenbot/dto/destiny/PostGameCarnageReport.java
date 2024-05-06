package com.deahtstroke.rivenbot.dto.destiny;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostGameCarnageReport {

  private Instant period;

  private Boolean activityWasStartedFromBeginning;

  private List<PGCREntry> entries;
}
