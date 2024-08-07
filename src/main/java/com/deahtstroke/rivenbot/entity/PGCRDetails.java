package com.deahtstroke.rivenbot.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "pgcr_details")
public class PGCRDetails {

  /**
   * The ID of the Post Game Carnage Report
   */
  @Id
  private String instanceId;

  /**
   * If the raid was started from the beginning
   */
  private Boolean fromBeginning;

  /**
   * List of players and some relevant information
   */
  private List<PlayerEntryDetails> players;
}
