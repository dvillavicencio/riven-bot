package com.deahtstroke.rivenbot.entity;

import lombok.Getter;

public enum ButtonStyle {
  BLURPLE(1),
  GREY(2),
  GREEN(3),
  RED(4);

  @Getter
  private final int buttonValue;

  ButtonStyle(int buttonValue) {
    this.buttonValue = buttonValue;
  }
}
