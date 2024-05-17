package com.deahtstroke.rivenbot.enums;

import lombok.Getter;

public enum SocialPlatform {

  DEVIANTART("DeviantArt", ":deviantart:", 1240125288885784616L),
  STEAM("Steam", ":steamlogo:", 1240765704114798635L),
  REDDIT("Reddit", "redditlogo", 1240767009910947861L),
  TWITTER("Twitter/X", "xlogo", 1240767798792294503L);

  @Getter
  private final String platformName;

  @Getter
  private final String emojiName;

  @Getter
  private final Long emojiId;

  SocialPlatform(String platformName, String emojiName, Long emojiId) {
    this.platformName = platformName;
    this.emojiName = emojiName;
    this.emojiId = emojiId;
  }
}
