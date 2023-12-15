package com.danielvm.destiny2bot.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilestoneResponse {

  /**
   * List of milestones from the Bungie API
   */
  private List<WeeklyActivity> weeklyActivities;
}
