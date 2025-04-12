package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token class for handling time duration values with units (e.g., 100ms, 2h, 30m, 1d)
 */
@PublicEvolving
public class TimeDuration extends Token {
  private static final Pattern TIME_DURATION_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)([nµm]?[s]|[mhdy]|ms)");
  private final long nanoseconds;

  public TimeDuration(String value) {
    super(TokenType.TIME_DURATION, value);
    this.nanoseconds = parseNanoseconds(value);
  }

  private long parseNanoseconds(String value) {
    Matcher matcher = TIME_DURATION_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + value);
    }

    double number = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toLowerCase();

    switch (unit) {
      case "ns":
        return (long) number;
      case "µs":
        return (long) (number * 1000);
      case "ms":
        return (long) (number * 1000 * 1000);
      case "s":
        return (long) (number * 1000 * 1000 * 1000);
      case "m":
        return (long) (number * 60 * 1000 * 1000 * 1000);
      case "h":
        return (long) (number * 60 * 60 * 1000 * 1000 * 1000);
      case "d":
        return (long) (number * 24 * 60 * 60 * 1000 * 1000 * 1000);
      case "y":
        return (long) (number * 365 * 24 * 60 * 60 * 1000 * 1000 * 1000);
      default:
        throw new IllegalArgumentException("Unsupported time duration unit: " + unit);
    }
  }

  public long getNanoseconds() {
    return nanoseconds;
  }

  public double getMicroseconds() {
    return nanoseconds / 1000.0;
  }

  public double getMilliseconds() {
    return nanoseconds / (1000.0 * 1000);
  }

  public double getSeconds() {
    return nanoseconds / (1000.0 * 1000 * 1000);
  }

  public double getMinutes() {
    return nanoseconds / (60.0 * 1000 * 1000 * 1000);
  }

  public double getHours() {
    return nanoseconds / (60.0 * 60 * 1000 * 1000 * 1000);
  }

  public double getDays() {
    return nanoseconds / (24.0 * 60 * 60 * 1000 * 1000 * 1000);
  }

  public double getYears() {
    return nanoseconds / (365.0 * 24 * 60 * 60 * 1000 * 1000 * 1000);
  }
} 