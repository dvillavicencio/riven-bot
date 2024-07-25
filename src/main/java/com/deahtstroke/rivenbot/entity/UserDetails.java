package com.deahtstroke.rivenbot.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_details")
public class UserDetails implements Serializable {

  @Id
  private String id;

  /**
   * The username portion for this player
   */
  private String username;

  /**
   * The userTag portion for this player
   */
  private String userTag;

  /**
   * Whether this user has a clan or not it will be saved in this field
   */
  private String destinyClanName;

  /**
   * The last time this user was requested
   */
  private Instant lastRequestDateTime;

  /**
   * Collection of user raid data
   */
  private Collection<UserRaidDetails> userRaidDetails;
}
