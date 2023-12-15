package com.danielvm.destiny2bot.dto.destiny;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class BungieErrorDto {

  @JsonAlias("ErrorCode")
  private String errorCode;

  @JsonAlias("ThrottleSeconds")
  private Long throttleSeconds;

  @JsonAlias("ErrorStatus")
  private String errorStatus;

  @JsonAlias("Message")
  private String message;

  @JsonAlias("MessageData")
  private Object messageData;

}
