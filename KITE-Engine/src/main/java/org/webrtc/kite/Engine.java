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

import org.apache.log4j.Logger;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.EndPoint;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.exception.*;

import javax.json.JsonException;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Entry point of the program
 */
public class Engine {
  
  private static final Logger logger = Logger.getLogger(Engine.class.getName());
  
  static {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HHmmss");
    System.setProperty("current.date", dateFormat.format(new Date()));
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
    
    try {
      Configurator.getInstance().setConfigFilePath(args[0]);
      Configurator.getInstance().buildConfig();
      Configurator.getInstance().setTimeStamp();
      for (TestConf testConf : (List<TestConf>) Configurator.getInstance().getConfigHandler()
        .getTestList()) {
        try {
          logger.info("Running " + testConf + " ...");
      
          List<List<EndPoint>> tupleList = Configurator.getInstance()
            .buildTuples(testConf.getTupleSize(), testConf.isPermute(), testConf.isRegression());
      
          List<Future<Object>> listOfResults = new MatrixRunner(testConf, tupleList).run();
      
          if (listOfResults != null) {
            StringBuilder testResults = new StringBuilder("The following are results for " + testConf + ":\n");
            for (Future<Object> future : listOfResults) {
              try {
                testResults.append("\r\n").append(future.get().toString());
              } catch (Exception e) {
                logger.error("Exception while test execution", e);
              }
            }
            testResults.append("\r\nEND OF RESULTS\r\n");
            logger.info("Matrix Runner Completed");
            logger.debug(testResults.toString());
          } else {
            logger.warn("No test case was found.");
          }
        } catch (Exception e) {
          logger.fatal("Error [Interruption]: The execution has been interrupted with the "
            + "following error: " + e.getLocalizedMessage(), e);
        }
      }
    } catch (FileNotFoundException e) {
      logger
        .fatal("Error [File Not Found]: '" + args[0] + "' either doesn't exist or is not a file.",
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
    } catch (KiteUnsupportedIntervalException e) {
      logger.fatal(
        "Error [Unrecognized interval]: '" + e.getIntervalName() + "' is unrecognized to KITE.",
        e);
    } catch (KiteInsufficientValueException e) {
      logger.fatal("Error [Config Parsing]: " + e.getLocalizedMessage(), e);
    } catch (KiteUnsupportedRemoteException e) {
      logger.fatal(
        "Error [Unrecognized remote]: '" + e.getRemoteName() + "' is unrecognized to KITE.", e);
    } catch (Exception e) {
      logger.fatal("FATAL Error: KITE has failed to start execution", e);
    }
    
  }
  
}
