/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.webrtc.kite.scheduler;

import org.webrtc.kite.exception.KiteUnsupportedIntervalException;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of supported intervals.
 */
public enum Interval {

  /**
   * Hourly interval.
   */
  HOURLY(1), /**
   * Daily interval.
   */
  DAILY(24), /**
   * Weekly interval.
   */
  WEEKLY(24 * 7);

  private final int value;

  /**
   * Constructs a new Interval with a given value.
   *
   * @param value an integer representing hours of interval.
   */
  private Interval(int value) {
    this.value = value;
  }

  /**
   * Value int.
   *
   * @return the int
   */
  public int value() {
    return value;
  }

  private static Map<String, Interval> supportedIntervalMap = new HashMap<String, Interval>();

  static {
    for (Interval interval : Interval.values()) {
      supportedIntervalMap.put(interval.name(), interval);
    }
  }

  /**
   * Checks whether the provided interval value is currently supported.
   *
   * @param name string interval value
   * @return the int
   * @throws KiteUnsupportedIntervalException if the 'name' is other than what is specified in Interval.
   */
  public static int interval(String name) throws KiteUnsupportedIntervalException {
    if (name == null) {
      return 0;
    }

    Interval interval = supportedIntervalMap.get(name);
    if (interval == null) {
      throw new KiteUnsupportedIntervalException(name);
    }

    return interval.value();
  }

}
