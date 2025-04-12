/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.api.parser;

import io.cdap.wrangler.api.annotations.PublicEvolving;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Token class for handling byte size values with units (e.g., 1KB, 2.5MB, 100GB)
 */
@PublicEvolving
public class ByteSize extends Token {
  private static final Pattern BYTE_SIZE_PATTERN = Pattern.compile("([0-9]+(?:\\.[0-9]+)?)([KkMmGgTtPpEe]?[Bb])");
  private final long bytes;

  public ByteSize(String value) {
    super(TokenType.BYTE_SIZE, value);
    this.bytes = parseBytes(value);
  }

  private long parseBytes(String value) {
    Matcher matcher = BYTE_SIZE_PATTERN.matcher(value);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + value);
    }

    double number = Double.parseDouble(matcher.group(1));
    String unit = matcher.group(2).toUpperCase();

    switch (unit) {
      case "B":
        return (long) number;
      case "KB":
        return (long) (number * 1024);
      case "MB":
        return (long) (number * 1024 * 1024);
      case "GB":
        return (long) (number * 1024 * 1024 * 1024);
      case "TB":
        return (long) (number * 1024L * 1024 * 1024 * 1024);
      case "PB":
        return (long) (number * 1024L * 1024 * 1024 * 1024 * 1024);
      case "EB":
        return (long) (number * 1024L * 1024 * 1024 * 1024 * 1024 * 1024);
      default:
        throw new IllegalArgumentException("Unsupported byte size unit: " + unit);
    }
  }

  public long getBytes() {
    return bytes;
  }

  public double getKB() {
    return bytes / 1024.0;
  }

  public double getMB() {
    return bytes / (1024.0 * 1024);
  }

  public double getGB() {
    return bytes / (1024.0 * 1024 * 1024);
  }

  public double getTB() {
    return bytes / (1024.0 * 1024 * 1024 * 1024);
  }

  public double getPB() {
    return bytes / (1024.0 * 1024 * 1024 * 1024 * 1024);
  }

  public double getEB() {
    return bytes / (1024.0 * 1024 * 1024 * 1024 * 1024 * 1024);
  }
} 