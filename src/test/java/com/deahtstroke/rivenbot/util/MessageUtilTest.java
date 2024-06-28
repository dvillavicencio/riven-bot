package com.deahtstroke.rivenbot.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.regex.Pattern;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class MessageUtilTest {

  @Test
  @DisplayName("Formatting date works correctly")
  void formatDateWorksAsExpected() {
    // given: any local date
    LocalDate localDate = LocalDate.of(2023, 1, 1);

    // when: format date is called
    String date = MessageUtils.formatDate(localDate);

    // then: the formatted string is correct
    assertThat(date)
        .isEqualTo("Sunday 1st, January 2023");
  }

  @ParameterizedTest
  @ValueSource(ints = {4, 5, 11, 12, 13, 20, 24})
  @DisplayName("Formatting date works correctly for suffix 'th'")
  void formatDateWorksWorksForSuffixTh(int dayNumber) {
    // given: any local date
    LocalDate localDate = LocalDate.of(2023, 1, dayNumber);
    Pattern pattern = Pattern.compile("(\\w*) %sth, January 2023".formatted(dayNumber));

    // when: format date is called
    String date = MessageUtils.formatDate(localDate);

    // then: the formatted string is correct
    assertThat(date).matches(pattern);
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 21})
  @DisplayName("Formatting date works correctly for suffix 'st'")
  void formatDateWorksWorksForSuffixSt(int dayNumber) {
    // given: any local date
    LocalDate localDate = LocalDate.of(2023, 1, dayNumber);
    Pattern pattern = Pattern.compile("(\\w*) %sst, January 2023".formatted(dayNumber));

    // when: format date is called
    String date = MessageUtils.formatDate(localDate);

    // then: the formatted string is correct
    assertThat(date).matches(pattern);
  }

  @ParameterizedTest
  @ValueSource(ints = {3, 23})
  @DisplayName("Formatting date works correctly for suffix 'rd'")
  void formatDateWorksWorksForSuffix(int dayNumber) {
    // given: any local date
    LocalDate localDate = LocalDate.of(2023, 1, dayNumber);
    Pattern pattern = Pattern.compile("(\\w*) %srd, January 2023".formatted(dayNumber));

    // when: format date is called
    String date = MessageUtils.formatDate(localDate);

    // then: the formatted string is correct
    assertThat(date).matches(pattern);
  }
}
