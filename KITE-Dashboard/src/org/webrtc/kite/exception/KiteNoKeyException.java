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

package org.webrtc.kite.exception;

/**
 * The KiteNoKeyException is thrown if the specified key is missing.
 */
public class KiteNoKeyException extends NullPointerException {

  private static final long serialVersionUID = 772908462246522793L;
  private String key;

  /**
   * Constructs a KiteNoKeyException with the missing key.
   *
   * @param key key
   */
  public KiteNoKeyException(String key) {
    super();
    this.key = key;
  }

  public String getKey() {
    return key;
  }

}
