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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.json.JsonException;
import org.apache.log4j.Logger;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.TestConf;
import org.webrtc.kite.exception.KiteBadValueException;
import org.webrtc.kite.exception.KiteInsufficientValueException;
import org.webrtc.kite.exception.KiteNoKeyException;
import org.webrtc.kite.exception.KiteUnsupportedRemoteException;

/**
 * Entry point of the program
 */
public class Engine {

  private final static Logger logger = Logger.getLogger(Engine.class.getName());

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
      Configurator.getInstance().buildConfig(new File(args[0]));

      Configurator.getInstance().setTimeStamp();

      for (TestConf testConf : Configurator.getInstance().getTestList()) {

        logger.info("Running " + testConf + " ...");

        List<Future<Object>> listOfResults = new MatrixRunner(testConf,
            Configurator.getInstance().buildTuples(testConf.getTupleSize()), testConf.getName())
                .run();

        logger.info("\nThe following are results for " + testConf + ":\n");
        if (listOfResults != null) {
          for (Future<Object> future : listOfResults)
            try {
              logger.info(future.get().toString());
            } catch (Exception e) {
              Utility.printStackTrace(e);
            }
        } else {
          logger.error("No test case found");
        }
      }
    } catch (FileNotFoundException e) {
      logger.error(
          "Error [File Not Found]: '" + args[0] + "' either doesn't exist or is not a file.");
      Utility.printStackTrace(e);
    } catch (JsonException | IllegalStateException e) {
      logger.error(
          "Error [Config Parsing]: Unable to parse the provided configuration file with the following error: "
              + e.getLocalizedMessage());
      Utility.printStackTrace(e);
    } catch (KiteNoKeyException e) {
      logger.error("Error [Config Parsing]: '" + e.getKey()
          + "' is not found in the configuration file or is null.");
      Utility.printStackTrace(e);
    } catch (KiteBadValueException e) {
      logger.error("Error [Config Parsing]: '" + e.getKey()
          + "' has an inappropriate value in the configuration file.");
      Utility.printStackTrace(e);
    } catch (KiteInsufficientValueException e) {
      logger.error("Error [Config Parsing]: " + e.getLocalizedMessage());
      Utility.printStackTrace(e);
    } catch (KiteUnsupportedRemoteException e) {
      logger.error(
          "Error [Unrecognized remote]: '" + e.getRemoteName() + "' is unrecognized to KITE.");
      Utility.printStackTrace(e);
    } catch (InterruptedException e) {
      logger.error(
          "Error [Interruption]: The execution has been interrupted with the following error: "
              + e.getLocalizedMessage());
      Utility.printStackTrace(e);
    } catch (ExecutionException e) {
      logger.error("Error [Execution]: The execution has been ended with the following error: "
          + e.getLocalizedMessage());
      Utility.printStackTrace(e);
    }

  }

}
