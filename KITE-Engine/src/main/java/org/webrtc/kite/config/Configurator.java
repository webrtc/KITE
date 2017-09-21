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

import org.apache.log4j.Logger;
import org.webrtc.kite.Utility;
import org.webrtc.kite.exception.KiteBadValueException;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteUnsupportedRemoteException;
import org.webrtc.kite.grid.RemoteAddressManager;
import org.webrtc.kite.grid.RemoteGridFetcher;
import javax.json.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Representation of the config file as a singleton.
 * <p>
 * {
 * "name": "config_name",
 * "callback": "http://localhost:8080/kiteweb/datacenter",
 * "remotes": [],
 * "tests": [],
 * "browsers": []
 * }
 */
public class Configurator {

    private static final Logger logger = Logger.getLogger(Configurator.class.getName());

    /* Singleton boiler plate code */
    private static Configurator instance = new Configurator();

    public static Configurator getInstance() {
        return instance;
    }

    private Configurator() {
    }
    /* Singleton boiler plate code */

    private long timeStamp = System.currentTimeMillis();

    private String name;
    private List<TestConf> testList;
    private List<Browser> browserList;

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
    }

    public String getName() {
        return this.name;
    }

    public List<TestConf> getTestList() {
        return this.testList;
    }

    public List<Browser> getBrowserList() {
        return this.browserList;
    }

    /**
     * Builds itself based on the content of the config file.
     *
     * @param file a File representation of the config file.
     * @throws FileNotFoundException          if the file is not found on provided path.
     * @throws JsonException                  if the content of the file is not a valid json.
     * @throws IllegalStateException          thrown by JsonReader's readObject().
     * @throws KiteNoKeyException             if any of the mandatory key (name, remotes, tests, browsers) is not found in the config file.
     * @throws KiteBadValueException          if the value of any key is invalid.
     * @throws KiteInsufficientValueException if the number of remotes, tests and browsers is less than 1.
     * @throws KiteUnsupportedRemoteException if an unsupported remote is found in 'remotes'.
     */
    public void buildConfig(File file) throws FileNotFoundException, JsonException, IllegalStateException, KiteNoKeyException, KiteBadValueException, KiteInsufficientValueException, KiteUnsupportedRemoteException {

        FileReader fileReader = null;
        JsonReader jsonReader = null;
        JsonObject jsonObject = null;
        try {
            fileReader = new FileReader(file);
            jsonReader = Json.createReader(fileReader);
            jsonObject = jsonReader.readObject();
        } finally {
            if (fileReader != null)
                try {
                    fileReader.close();
                } catch (IOException e) {
                    logger.warn(e.getMessage(), e);
                }
            if (jsonReader != null)
                jsonReader.close();
        }

        this.name = (String) Utility.throwNoKeyOrBadValueException(jsonObject, "name", String.class);

        String callbackURL = jsonObject.getString("callback", null);

        List<JsonObject> jsonObjectList = (List<JsonObject>) Utility.throwNoKeyOrBadValueException(jsonObject, "remotes", JsonArray.class);
        if (jsonObjectList.size() < 1)
            throw new KiteInsufficientValueException("Remote objects are less than one.");

        RemoteManager remoteManager = new RemoteManager(jsonObjectList);

        jsonObjectList = (List<JsonObject>) Utility.throwNoKeyOrBadValueException(jsonObject, "tests", JsonArray.class);
        if (jsonObjectList.size() < 1)
            throw new KiteInsufficientValueException("Test objects are less than one.");

        this.testList = new ArrayList<TestConf>();
        for (JsonObject object : jsonObjectList)
            this.testList.add(new TestConf(callbackURL, object));

        jsonObjectList = (List<JsonObject>) Utility.throwNoKeyOrBadValueException(jsonObject, "browsers", JsonArray.class);
        if (jsonObjectList.size() < 1)
            throw new KiteInsufficientValueException("Browser objects are less than one.");

        logger.info("Finished reading the configuration file.");

        this.adjustRemotes(remoteManager, jsonObjectList);
    }

    /**
     * Builds the browser list and sets the remote address in each of the browser object.
     * <p>
     * The algorithm is as follows:
     * 1) If there is only one remote provided then sets that remote for every browser.
     * 2) If there are more than one remotes then query all remotes against the provided browsers in sequential order to check if a remote can spawn the browser.
     * 3) If a browser is not supported by a remote then set 'local' as its remote if provided otherwise set the top remote from the remote array.
     *
     * @param remoteManager  RemoteManager
     * @param jsonObjectList an implementation of List<JsonObject>.
     */
    private void adjustRemotes(RemoteManager remoteManager, List<JsonObject> jsonObjectList) {

        Set<Browser> set = new LinkedHashSet<Browser>();

        List<Remote> remoteList = remoteManager.getRemoteList();
        int remoteListSize = remoteList.size();

        if (remoteListSize == 1) {
            String remoteAddress = remoteList.get(0).getRemoteAddress();
            for (JsonObject object : jsonObjectList) {
                Browser browser = new Browser(null, object);
                if (browser.getRemoteAddress() == null){
                    browser.setRemoteAddress(remoteAddress);
                }
                set.add(browser);
            }
        } else {
            int index = 0;
            Remote defaultRemote = remoteList.get(0);
            if (defaultRemote.isLocal()) index = 1;

            List<RemoteGridFetcher> fetcherList = new ArrayList<>();
            for (; index < remoteListSize; index++)
                fetcherList.add(remoteList.get(index).getGridFetcher());

            RemoteAddressManager remoteAddressManager = new RemoteAddressManager(fetcherList);
            remoteAddressManager.communicateWithRemotes();

            for (JsonObject object : jsonObjectList) {
                Browser browser = new Browser(null, object);
                if (browser.getRemoteAddress() == null){
                String remoteAddress = remoteAddressManager.findAppropriateRemoteAddress(browser);
                browser.setRemoteAddress(remoteAddress == null ? defaultRemote.getRemoteAddress() : remoteAddress);
                }
                set.add(browser);
            }
        }

        this.browserList = new ArrayList<Browser>(set);
    }

    /**
     * Creates a matrix of browser tuples.
     *
     * @param tupleSize tuple size
     * @return a matrix of browser tuples as List<List<Browser>>
     */
    public List<List<Browser>> buildTuples(int tupleSize) {

        List<List<Browser>> listOfBrowserList = new ArrayList<List<Browser>>();

        double totalTuples = Math.pow(this.browserList.size(), tupleSize);

        logger.info(totalTuples + " test cases to run.");

        for (int i = 0; i < totalTuples; i++)
            listOfBrowserList.add(new ArrayList<Browser>());

        for (int i = 0; i < tupleSize; i++) {
            double marge = totalTuples / Math.pow(this.browserList.size(), i + 1);
            double rep = Math.pow(this.browserList.size(), i);
            for (int x = 0; x < rep; x++)
                for (int j = 0; j < this.browserList.size(); j++)
                    for (int k = 0; k < marge; k++)
                        (listOfBrowserList.get((int) (x * totalTuples / rep + j * marge + k))).add(i, new Browser(this.browserList.get(j)));
        }

        return listOfBrowserList;
    }

    /**
     * Returns a JsonArrayBuilder based on the browser list.
     *
     * @return JsonArrayBuilder
     */
    public JsonArrayBuilder getBrowserListJsonArray() {
        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        for (Browser browser : this.browserList)
            jsonArrayBuilder.add(browser.getJsonObjectBuilder());
        return jsonArrayBuilder;
    }

}
