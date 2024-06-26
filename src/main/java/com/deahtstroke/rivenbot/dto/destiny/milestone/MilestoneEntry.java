package com.deahtstroke.rivenbot.dto.destiny.milestone;

import java.time.ZonedDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MilestoneEntry {

  private Long milestoneHash;

  private ZonedDateTime startDate;

  private ZonedDateTime endDate;

  private List<ActivitiesDto> activities;
}
