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

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.listeners.JobListenerSupport;

/**
 * A JobListener to shut the scheduler down after it is finished.
 */
public class MatrixRunnerJobListener extends JobListenerSupport {

  private static final Logger logger = Logger.getLogger(MatrixRunnerJobListener.class.getName());

  @Override public String getName() {
    return MatrixRunnerJobListener.class.getName();
  }

  @Override
  public void jobWasExecuted(JobExecutionContext jobExecutionContext, JobExecutionException e) {
    // Clear and Shutdown the scheduler.
    if (jobExecutionContext.getJobDetail().getJobDataMap().getBoolean("shouldShutdown")) {
      Scheduler scheduler = jobExecutionContext.getScheduler();
      try {
        scheduler.clear();
      } catch (SchedulerException se) {
        logger.error("Exception while clearing the Scheduler", se);
      }
      try {
        scheduler.shutdown();
      } catch (SchedulerException se) {
        logger.error("Exception while shutting down the Scheduler", se);
      }
    }
  }

}
