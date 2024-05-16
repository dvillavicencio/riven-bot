package com.deahtstroke.rivenbot.dto;

import com.deahtstroke.rivenbot.enums.SocialPlatform;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SocialLink {

  /**
   * Social Platform this link belongs to
   */
  private SocialPlatform socialPlatform;

  /**
   * Social media hyperlink
   */
  private String socialLink;

}
