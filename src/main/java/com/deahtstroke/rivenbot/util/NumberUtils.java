package com.deahtstroke.rivenbot.util;

public class NumberUtils {

  private NumberUtils() {
  }

  /**
   * Determines whether the given parameter is an Integer
   *
   * @param param the parameter to check
   * @return true if its an integer, else false
   */
  public static boolean isInteger(String param) {
    try {
      Integer.parseInt(param);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * Determines whether the number contains all the digits or not
   *
   * @param number the number to check
   * @param digits the digits to find
   * @return true if it contains the digits, false if not
   */
  public static boolean contains(Integer number, Integer digits) {
    if (number < 0) {
      return false;
    }
    int[] countA = new int[10];
    int[] countB = new int[10];

    while (number > 0) {
      countA[number % 10]++;
      number /= 10;
    }

    while (digits > 0) {
      countB[digits % 10]++;
      digits /= 10;
    }

    for (int i = 0; i < 10; i++) {
      if (countA[i] > countB[i]) {
        return false;
      }
    }
    return true;
  }
}
