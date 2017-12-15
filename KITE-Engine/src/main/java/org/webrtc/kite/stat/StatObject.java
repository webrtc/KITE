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

package org.webrtc.kite.stat;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * Parent class for any WebRTC stat object.
 */
public abstract class StatObject {
    private String id;

    /**
     * Returns JsonObjectBuilder.
     *
     * @return JsonObjectBuilder
     */
    public abstract JsonObjectBuilder getJsonObjectBuilder();

    /**
     * Returns a JsonObject representation.
     *
     * @return JsonObject
     */
    public JsonObject getJsonObject() {
        return this.getJsonObjectBuilder().build();
    }

    @Override
    public String toString() {
        return this.getJsonObject().toString();
    }


    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
