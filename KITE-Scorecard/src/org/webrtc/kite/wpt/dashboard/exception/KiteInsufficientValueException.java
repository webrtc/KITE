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

package org.webrtc.kite.wpt.dashboard.exception;

/**
 * The KiteInsufficientValueException is thrown if the value of a key is less than its expected
 * value.
 */
public class KiteInsufficientValueException extends Exception {

  private static final long serialVersionUID = -1788678025516435737L;

  /**
   * Constructs a KiteInsufficientValueException with the specified detailed message.
   *
   * @param message message
   */
  public KiteInsufficientValueException(String message) {
    super(message);
  }

}
