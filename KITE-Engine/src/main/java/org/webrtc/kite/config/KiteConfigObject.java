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

package org.webrtc.kite.config;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Parent class for any object defined in the config file.
 */
public abstract class KiteConfigObject implements Cloneable {

  /**
   * Returns JsonObjectBuilder.
   *
   * @return JsonObjectBuilder json object builder
   */
  abstract public JsonObjectBuilder getJsonObjectBuilder();

  /**
   * JsonObjectBuilder primarily for result json construction.
   *
   * @return JsonObjectBuilder json object builder for result
   */
  abstract public JsonObjectBuilder getJsonObjectBuilderForResult();

  /**
   * Returns a JsonObject representation.
   *
   * @return JsonObject json object
   */
  public JsonObject getJsonObject() {
    return this.getJsonObjectBuilder().build();
  }

  /**
   * Clone of the object.
   *
   * @return clone of the object
   * @throws CloneNotSupportedException the clone not supported exception
   */
  public KiteConfigObject getClone() throws CloneNotSupportedException {
    return (KiteConfigObject) this.clone();
  }

  @Override public String toString() {
    return this.getJsonObject().toString();
  }

}
