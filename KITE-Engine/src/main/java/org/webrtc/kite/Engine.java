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
import org.webrtc.kite.config.Tuple;
import org.webrtc.kite.exception.*;

import javax.json.JsonException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
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
  
  private static final Logger logger = Logger.getLogger(Engine.class.getName());
  
  
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
    
    buildConfig(args[0]);

    for (TestConf testConf : Configurator.getInstance().getConfigHandler().getTestList()) {
      run(testConf);
    }
  }
  
  public static void run(TestConf testConf) {
    ExecutorService service = Executors.newSingleThreadExecutor();
    try {
      List<Tuple> tupleList = Configurator.getInstance()
        .buildTuples(testConf.getTupleSize(), testConf.isPermute(), testConf.isRegression());
      
      service.submit(new TestRunThread(testConf, tupleList)).get();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      service.shutdown();
    }
  }
  
  public static void buildConfig(String pathToConfigFile) {
    try {
      Configurator.getInstance().setConfigFilePath(pathToConfigFile);
      Configurator.getInstance().buildConfig();
      Configurator.getInstance().setTimeStamp();
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
    }  catch (KiteInsufficientValueException e) {
      logger.fatal("Error [Config Parsing]: " + e.getLocalizedMessage(), e);
    } catch (KiteUnsupportedRemoteException e) {
      logger.fatal(
        "Error [Unrecognized remote]: '" + e.getRemoteName() + "' is unrecognized to KITE.", e);
    } catch (Exception e) {
      logger.fatal("FATAL Error: KITE has failed to start execution", e);
    }
  }
}
