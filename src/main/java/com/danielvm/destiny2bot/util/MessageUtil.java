package com.danielvm.destiny2bot.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;


public class MessageUtil {

  private static final LocalTime DESTINY_2_STANDARD_RESET_TIME = LocalTime.of(9, 0);
  private static final ZoneId STANDARD_TIMEZONE = ZoneId.of("America/Los_Angeles");
  public static final ZonedDateTime NEXT_TUESDAY = ZonedDateTime.of(
          LocalDate.now(STANDARD_TIMEZONE), DESTINY_2_STANDARD_RESET_TIME, STANDARD_TIMEZONE)
      .with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
  public static final ZonedDateTime PREVIOUS_TUESDAY = ZonedDateTime.of(
          LocalDate.now(STANDARD_TIMEZONE), DESTINY_2_STANDARD_RESET_TIME, STANDARD_TIMEZONE)
      .with(TemporalAdjusters.previous(DayOfWeek.TUESDAY));

  /**
   * Formatter used to format the date for a message
   */
  public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEEE MMMM d");

  private MessageUtil() { }

}
