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

import org.webrtc.kite.exception.KiteInsufficientValueException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 * Representation of a test object in the config file.
 * <p>
 * {
 * "name": "IceConnectionTest",
 * "tupleSize": 2                         ,
 * "testImpl": "org.webrtc.kite.IceConnectionTest"
 * }
 */
public class TestConf extends KiteConfigObject {

    private String name;
    private int tupleSize;
    private String testImpl;
    private int noOfThreads;
    private int maxRetryCount;
    private JsonValue payload;
    private String callbackURL;
    private String description;

    /**
     * Constructs a new TestConf with the given callback url and JsonObject.
     *
     * @param callbackURL a string representation of callback url.
     * @param jsonObject  JsonObject
     */
    public TestConf(String callbackURL, JsonObject jsonObject) throws KiteInsufficientValueException {
        this.name = jsonObject.getString("name");
        this.tupleSize = jsonObject.getInt("tupleSize");
        this.testImpl = jsonObject.getString("testImpl");

        this.noOfThreads = jsonObject.getInt("noOfThreads", 1);
        if (this.noOfThreads < 1)
            throw new KiteInsufficientValueException("noOfThreads for " + this.name + " is less than one.");

        this.maxRetryCount = jsonObject.getInt("maxRetryCount", 1);
        if (this.maxRetryCount < 0)
            throw new KiteInsufficientValueException("maxRetryCount for " + this.name + " is a negative value.");

        this.payload = jsonObject.getOrDefault("payload", null);

        // Override the global value with the local value
        this.callbackURL = jsonObject.getString("callback", null);
        if (this.callbackURL == null) this.callbackURL = callbackURL;

        this.description = jsonObject.getString("description", "No description was provided fot this test.");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTupleSize() {
        return tupleSize;
    }

    public void setTupleSize(int tupleSize) {
        this.tupleSize = tupleSize;
    }

    public String getTestImpl() {
        return testImpl;
    }

    public void setTestImpl(String testImpl) {
        this.testImpl = testImpl;
    }

    public int getNoOfThreads() { return noOfThreads; }

    public void setNoOfThreads(int noOfThreads) { this.noOfThreads = noOfThreads; }

    public int getMaxRetryCount() { return maxRetryCount; }

    public void setMaxRetryCount(int maxRetryCount) { this.maxRetryCount = maxRetryCount; }

    public JsonValue getPayload() { return payload; }

    public void setPayload(JsonValue payload) { this.payload = payload; }

    public String getCallbackURL() { return callbackURL; }

    public void setCallbackURL(String callbackURL) {
        this.callbackURL = callbackURL;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns an identifier for the TestConf in the format: name + "_" + last four digits of the Configurator's timestamp + "_" + index.
     *
     * @param index Index of the testcase in the array.
     * @return Remote test identifier
     */
    public String getRemoteTestIdentifier(int index) {
        String identifier = "" + Configurator.getInstance().getTimeStamp();
        identifier = identifier.substring(identifier.length() - 4);
        return name + "_" + identifier + "_" + index;
    }

    @Override
    public JsonObjectBuilder getJsonObjectBuilder() {
        return Json.createObjectBuilder()
                .add("name", this.getName())
                .add("tupleSize", this.getTupleSize())
                .add("testImpl", this.getTestImpl());
    }

    @Override
    public JsonObjectBuilder getJsonObjectBuilderForResult() {
        return Json.createObjectBuilder()
                .add("timeStamp", Configurator.getInstance().getTimeStamp())
                .add("configName", Configurator.getInstance().getName())
                .add("testName", this.getName())
                .add("tupleSize", this.getTupleSize())
                .add("testImpl", this.getTestImpl());
    }

}
