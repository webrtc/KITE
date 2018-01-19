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
import javax.json.JsonException;

import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.KeyMatcher;
import org.webrtc.kite.config.Configurator;
import org.webrtc.kite.exception.*;
import org.webrtc.kite.scheduler.MatrixRunnerJob;
import org.webrtc.kite.scheduler.MatrixRunnerJobListener;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Entry point of the program
 */
public class Engine {

    private final static String IDENTITY_GROUP = "KITE";
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

        Scheduler scheduler = null;
        try {
            Configurator.getInstance().buildConfig(new File(args[0]));
            int interval = Configurator.getInstance().getInterval();

            // Grab the Scheduler instance from the Factory
            scheduler = StdSchedulerFactory.getDefaultScheduler();
            // and start it off
            scheduler.start();

            // define the job and tie it to our MatrixRunnerJob class
            JobDetail job = newJob(MatrixRunnerJob.class)
                    .withIdentity(MatrixRunnerJob.class.getName(), Engine.IDENTITY_GROUP)
                    .usingJobData("shouldShutdown", interval <= 0)
                    .build();

            // Trigger the job to run now, and then repeat every 'interval' number of hours
            Trigger trigger = null;
            if (interval > 0) {
                trigger = newTrigger()
                        .withIdentity(MatrixRunnerJob.class.getName(), Engine.IDENTITY_GROUP)
                        .startNow()
                        .withSchedule(simpleSchedule()
                                .withIntervalInHours(interval)
                                .repeatForever())
                        .build();
            } else {
                trigger = newTrigger()
                        .withIdentity(MatrixRunnerJob.class.getName(), Engine.IDENTITY_GROUP)
                        .startNow()
                        .build();
                // Adding MatrixRunnerJobListener to shutdown the scheduler after the job is finished.
                scheduler.getListenerManager().addJobListener(new MatrixRunnerJobListener(), KeyMatcher.keyEquals(new JobKey(MatrixRunnerJob.class.getName(), Engine.IDENTITY_GROUP)));
            }

            // Tell quartz to schedule the job using the trigger
            scheduler.scheduleJob(job, trigger);

        } catch (FileNotFoundException e) {
            logger.error(
                    "Error [File Not Found]: '" + args[0] + "' either doesn't exist or is not a file.", e);
        } catch (JsonException | IllegalStateException e) {
            logger.error(
                    "Error [Config Parsing]: Unable to parse the provided configuration file with the following error: "
                            + e.getLocalizedMessage(), e);
        } catch (KiteNoKeyException e) {
            logger.error("Error [Config Parsing]: '" + e.getKey()
                    + "' is not found in the configuration file or is null.", e);
        } catch (KiteBadValueException e) {
            logger.error("Error [Config Parsing]: '" + e.getKey()
                    + "' has an inappropriate value in the configuration file.", e);
        } catch (KiteUnsupportedIntervalException e) {
            logger.error("Error [Unrecognized interval]: '" + e.getIntervalName()
                    + "' is unrecognized to KITE.", e);
        } catch (KiteInsufficientValueException e) {
            logger.error("Error [Config Parsing]: " + e.getLocalizedMessage(), e);
        } catch (KiteUnsupportedRemoteException e) {
            logger.error(
                    "Error [Unrecognized remote]: '" + e.getRemoteName() + "' is unrecognized to KITE.", e);
        } catch (SchedulerException e) {
            logger.error(
                    "FATAL Error: KITE has failed to start execution.", e);
            try {
                if (scheduler != null && (scheduler.isStarted() || !scheduler.isShutdown())) {
                    try {
                        scheduler.clear();
                    } catch (SchedulerException se) {

                    }
                    try {
                        scheduler.shutdown();
                    } catch (SchedulerException se) {

                    }
                }
            } catch (SchedulerException se) {

            }
        }

    }

}
