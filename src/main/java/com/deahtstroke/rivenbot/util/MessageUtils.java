package com.deahtstroke.rivenbot.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import org.springframework.validation.annotation.Validated;

@Validated
public class MessageUtils {

  public static final Integer EPHEMERAL_BYTE = 1000000;
  public static final String ICON_URL = "https://ih1.redbubble.net/image.2953200665.7291/st,small,507x507-pad,600x600,f8f8f8.jpg";
  public static final String GITHUB_REPO = "https://github.com/dvillavicencio/riven-bot";
  public static final String DISCORD_SERVER = "https://discord.gg/yMShmXQs";

  private static final LocalTime DESTINY_2_STANDARD_RESET_TIME = LocalTime.of(9, 0);
  private static final ZoneId STANDARD_TIMEZONE = ZoneId.of("America/Los_Angeles");

  public static final ZonedDateTime NEXT_TUESDAY = ZonedDateTime.of(
          LocalDate.now(STANDARD_TIMEZONE), DESTINY_2_STANDARD_RESET_TIME, STANDARD_TIMEZONE)
      .with(TemporalAdjusters.next(DayOfWeek.TUESDAY));
  public static final ZonedDateTime PREVIOUS_TUESDAY = ZonedDateTime.of(
          LocalDate.now(STANDARD_TIMEZONE), DESTINY_2_STANDARD_RESET_TIME, STANDARD_TIMEZONE)
      .with(TemporalAdjusters.previous(DayOfWeek.TUESDAY));
  /**
   * Formatter used to format the date for a message Example: the LocalDate object with date
   * 2023-01-01 would be formatted to "Sunday 1st, January 2023"
   */
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(
      "EEEE d'%s', MMMM yyyy");


  private MessageUtils() {
  }

  /**
   * Format the given local date using the class formatter, it also includes the correct suffix for
   * a date
   *
   * @param localDate the date to be formatted
   * @return String of the formatted local date
   */
  public static String formatDate(LocalDate localDate) {
    Integer dayOfMonth = localDate.getDayOfMonth();
    return FORMATTER.format(localDate)
        .formatted(suffix(dayOfMonth));
  }

  private static String suffix(Integer dayOfMonth) {
    if (dayOfMonth >= 11 && dayOfMonth <= 13) {
      return "th";
    }
    return switch (dayOfMonth % 10) {
      case 1 -> "st";
      case 2 -> "nd";
      case 3 -> "rd";
      default -> "th";
    };
  }


}
