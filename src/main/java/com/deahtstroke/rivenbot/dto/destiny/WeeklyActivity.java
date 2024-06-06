package com.deahtstroke.rivenbot.dto.destiny;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyActivity {

  /**
   * Name of the milestone
   */
  private String name;

  /**
   * Description of the milestone
   */
  private String description;

  /**
   * The start date for this milestone
   */
  private ZonedDateTime startDate;

  /**
   * The end date for this milestone
   */
  private ZonedDateTime endDate;

}
