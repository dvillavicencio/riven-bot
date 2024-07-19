package com.deahtstroke.rivenbot.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NumberUtilsTest {

  @Test
  @DisplayName("isInteger works and does not throw exception")
  void checkIfIsIntegerWorks() {
    // given: some input that is not convertible to number
    String hello = "hello";

    // when: isInteger called
    Boolean result = NumberUtils.isInteger(hello);

    // then: the result is false
    Assertions.assertThat(result)
        .isFalse();
  }

  @Test
  @DisplayName("isInteger works for convertible integer types")
  void checkIfIsIntegerWorksForIntegers() {
    // given: some input that is convertible to a number
    String input = "102";

    // when: isInteger called
    Boolean result = NumberUtils.isInteger(input);

    // then: the result is true
    Assertions.assertThat(result)
        .isTrue();
  }
}
