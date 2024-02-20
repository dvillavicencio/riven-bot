package com.danielvm.destiny2bot.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user_details")
public class UserDetails implements Serializable {

  /**
   * The user identifier for a user which is a combination of their global username and their global
   * tag, e.g., Deaht#8080
   */
  @Id
  private String userIdentifier;

  /**
   * Whether this user has a clan or not it will be saved in this field
   */
  private String destinyClanName;

  /**
   * The last time this user was requested
   */
  private LocalDateTime lastRequestDateTime;

  /**
   * Collection of user raid data
   */
  private Collection<UserRaidDetails> userRaidDetails;
}
