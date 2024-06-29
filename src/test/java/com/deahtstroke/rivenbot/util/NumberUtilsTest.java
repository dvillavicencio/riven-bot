package com.deahtstroke.rivenbot.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class NumberUtilsTest {

  @Test
  @DisplayName("Contains digit works when the digit is not found in the number")
  void digitNotFound() {
    // given: an integer and a digit to find
    int number = 217;
    int digit = 5;

    // when: containsDigit is called
    boolean result = NumberUtils.contains(number, digit);

    // then: the result is false
    Assertions.assertThat(result).isFalse();
  }

  @ParameterizedTest
  @ValueSource(ints = {2, 1, 7})
  @DisplayName("Contains digit works when the digit is found in the number #1")
  void digitFound(int digit) {
    // given: an integer and a digit to find
    int number = 217;

    // when: containsDigit is called
    boolean result = NumberUtils.contains(number, digit);

    // then: the result is true
    Assertions.assertThat(result).isTrue();
  }
}
