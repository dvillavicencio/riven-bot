package com.danielvm.destiny2bot.dto.destiny.milestone;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivitiesDto {

  private Long activityHash;

  private List<String> challengeObjectiveHashes;

}
