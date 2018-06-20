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

import org.quartz.Job;
import org.quartz.JobExecutionException;

/**
 * The type Kite job.
 */
public abstract class KiteJob implements Job {

  /**
   * Make up the grid.
   *
   * @throws JobExecutionException the job execution exception
   */
  protected void makeUpTheGrid() throws JobExecutionException { }

  /**
   * Make down the grid.
   */
  protected void makeDownTheGrid() { }

}
