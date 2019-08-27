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

package org.webrtc.kite;

import io.cosmosoftware.kite.report.KiteLogger;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.client.Client;
import org.webrtc.kite.config.paas.Paas;
import org.webrtc.kite.config.test.TestConfig;
import org.webrtc.kite.config.test.Tuple;
import org.webrtc.kite.exception.KiteBadValueException;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteNoKeyException;

import javax.json.JsonException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Entry point of the program
 */
public class Engine {

  static {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
    System.setProperty("current.date", dateFormat.format(new Date()));
  }

  private static final KiteLogger logger = KiteLogger.getLogger(Engine.class.getName());
  
  public static void buildConfig(Configurator configurator, String pathToConfigFile) {
    try {
      configurator.setConfigFilePath(pathToConfigFile);
      configurator.buildConfig();
      configurator.setTimeStamp();
    } catch (FileNotFoundException e) {
      logger
        .fatal("Error [File Not Found]: '" + pathToConfigFile + "' either doesn't exist or is not a file.",
          e);
    } catch (JsonException | IllegalStateException e) {
      logger.fatal("Error [Config Parsing]: Unable to parse the provided configuration "
        + "file with the following error: " + e.getLocalizedMessage(), e);
    } catch (KiteNoKeyException e) {
      logger.fatal("Error [Config Parsing]: '" + e.getKey()
        + "' is not found in the configuration file or is null.", e);
    } catch (KiteBadValueException e) {
      logger.fatal("Error [Config Parsing]: '" + e.getKey()
        + "' has an inappropriate value in the configuration file.", e);
    } catch (KiteInsufficientValueException e) {
      logger.fatal("Error [Config Parsing]: " + e.getLocalizedMessage(), e);
    } catch (Exception e) {
      logger.fatal("FATAL Error: KITE has failed to start execution", e);
    }
  }
  
  public static void distributeRemote(Configurator configurator, List<Tuple> tupleList) {
    // setting remote hub address to client, using circular linked list
    // each tuple will be prioritised to be in the same hub
    //need to handle mobile == null
    for (Tuple tuple : tupleList) {
      Paas paas = configurator.getRemoteList().get();
      for (Client client : tuple.getClients()) {
        if (client.getPaas() == null) {
          client.setPaas(paas);
        }
      }
    }
  }
  
  /**
   * main method
   *
   * @param args relative or absolute path of the configuration file.
   */
  public static void main(String[] args) {
    if (args.length < 1) {
      logger.error("Error [Missing Argument]: Use java -jar KITE.jar <absolute path/config.json>");
      return;
    }
    Configurator configurator = new Configurator();
    buildConfig(configurator, args[0]);

    for (TestConfig testConfig : configurator.getConfigHandler().getTestList()) {
      List<Tuple> tupleList = new ArrayList<>();
      List<List<Integer>> matrix = configurator.getMatrix();
      if (matrix != null && !matrix.isEmpty()) {
        for (List<Integer> indexList: matrix ) {
          Tuple tuple = new Tuple();
          for (int index: indexList) {
            tuple.add(configurator.getConfigHandler().getClientList().get(index));
          }
          tupleList.add(tuple);
        }
      } else {
        tupleList = configurator
          .buildTuples(testConfig.getTupleSize(), testConfig.isPermute(), testConfig.isRegression());
      }
      distributeRemote(configurator, tupleList);
      testConfig.setReportPath(configurator.getReportPath());
      ExecutorService service = Executors.newSingleThreadExecutor();
      try {
        runInterop(service, configurator.getName(), testConfig, tupleList);
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        service.shutdown();
      }
    }
  }
  
  
  public static List<Future<Object>> runInterop(ExecutorService service, String testSuiteName, TestConfig testConfig, List<Tuple> tupleList) throws ExecutionException, InterruptedException {
    TestRunThread runThread = new TestRunThread(testConfig, tupleList);
    runThread.setName(testSuiteName);
    return service.submit(runThread).get();
  }
}
