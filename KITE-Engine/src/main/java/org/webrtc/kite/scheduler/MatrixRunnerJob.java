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

package org.webrtc.kite.scheduler;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.webrtc.kite.MatrixRunner;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.config.TestConf;

/**
 * A Quartz job incapsulating Matrix Runner.
 */
public class MatrixRunnerJob extends KiteJob {

  private static final Logger logger = Logger.getLogger(MatrixRunnerJob.class.getName());

  @Override public void execute(JobExecutionContext jobExecutionContext)
      throws JobExecutionException {

    this.makeUpTheGrid();
    Configurator.getInstance().setTimeStamp();

    for (TestConf testConf : (List<TestConf>) Configurator.getInstance().getConfigHandler()
        .getTestList()) {
      try {
        if (logger.isInfoEnabled()) {
          logger.info("Running " + testConf + " ...");
        }

        List<Future<Object>> listOfResults = new MatrixRunner(testConf,
            Configurator.getInstance().buildTuples(testConf.getTupleSize()), testConf.getName())
            .run();

        if (listOfResults != null) {
          if (logger.isInfoEnabled()) {
            String testResults = "The following are results for " + testConf + ":\n";
            for (Future<Object> future : listOfResults) {
              try {
                testResults += "\r\n" + future.get().toString();
              } catch (Exception e) {
                logger.error("Exception while test execution", e);
              }
            }
            testResults += "\r\nEND OF RESULTS\r\n";
            logger.info(testResults);
          }
        } else {
          logger.warn("No test case was found.");
        }
      } catch (InterruptedException e) {
        logger.fatal("Error [Interruption]: The execution has been interrupted with the "
            + "following error: " + e.getLocalizedMessage(), e);
      } catch (ExecutionException e) {
        logger.fatal(
            "Error [Execution]: The execution has been ended with the following error: "
                + e.getLocalizedMessage(), e);
      }
    }

    this.makeDownTheGrid();

  }

}
