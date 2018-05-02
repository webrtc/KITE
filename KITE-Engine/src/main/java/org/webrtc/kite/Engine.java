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
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.exception.*;
import org.webrtc.kite.scheduler.MatrixRunnerJobListener;

import javax.json.JsonException;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Entry point of the program
 */
public class Engine {

  private static final Logger logger = Logger.getLogger(Engine.class.getName());

  private static final String IDENTITY_GROUP = "KITE";

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

    Scheduler scheduler = null;
    try {
      Configurator.getInstance().buildConfig(new File(args[0]));
      int interval = Configurator.getInstance().getInterval();

      // Grab the Scheduler instance from the Factory
      scheduler = StdSchedulerFactory.getDefaultScheduler();
      // and start it off
      scheduler.start();

      Class jobClass = Configurator.getInstance().getJobClass();
      // define the job and tie it to the KiteJob class
      JobDetail job = newJob(jobClass).withIdentity(jobClass.getName(), Engine.IDENTITY_GROUP)
          .usingJobData("shouldShutdown", interval <= 0).build();

      // Trigger the job to run now, and then repeat every 'interval' number of hours
      Trigger trigger = null;
      if (interval > 0) {
        trigger = newTrigger().withIdentity(jobClass.getName(), Engine.IDENTITY_GROUP).startNow()
            .withSchedule(simpleSchedule().withIntervalInHours(interval).repeatForever()).build();
      } else {
        trigger =
            newTrigger().withIdentity(jobClass.getName(), Engine.IDENTITY_GROUP).startNow().build();
        // Adding MatrixRunnerJobListener to shutdown the scheduler after the job is finished.
        scheduler.getListenerManager().addJobListener(new MatrixRunnerJobListener(),
            KeyMatcher.keyEquals(new JobKey(jobClass.getName(), Engine.IDENTITY_GROUP)));
      }

      // Tell quartz to schedule the job using the trigger
      scheduler.scheduleJob(job, trigger);

    } catch (FileNotFoundException e) {
      logger
          .fatal("Error [File Not Found]: '" + args[0]
                  + "' either doesn't exist or is not a file.",
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
          "Error [Unrecognized interval]: '" + e.getIntervalName()
              + "' is unrecognized to KITE.",
          e);
    } catch (KiteInsufficientValueException e) {
      logger.fatal("Error [Config Parsing]: " + e.getLocalizedMessage(), e);
    } catch (KiteUnsupportedRemoteException e) {
      logger.fatal(
          "Error [Unrecognized remote]: '" + e.getRemoteName()
              + "' is unrecognized to KITE.", e);
    } catch (Exception e) {
      logger.fatal("FATAL Error: KITE has failed to start execution", e);
      try {
        if (scheduler != null && (scheduler.isStarted() || !scheduler.isShutdown())) {
          try {
            scheduler.clear();
          } catch (SchedulerException se) {
            logger.warn("Exception while clearing the Scheduler", e);
          }
          try {
            scheduler.shutdown();
          } catch (SchedulerException se) {
            logger.warn("Exception while clearing the Scheduler", e);
          }
        }
      } catch (SchedulerException se) {
        logger.warn("Exception while getting the scheduler status", e);
      }
    }

  }

}
