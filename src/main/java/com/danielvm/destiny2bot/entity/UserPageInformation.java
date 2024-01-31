package com.danielvm.destiny2bot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPageInformation {

  private Long userDiscordId;

  private Integer numberOfPages;

  private Integer lastPageCount;
}
