package com.danielvm.destiny2bot.dto.destiny;

import java.time.ZonedDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activity {

  private ZonedDateTime period;

  private ActivityDetails activityDetails;

  private Map<String, ValueEntry> values;
}
