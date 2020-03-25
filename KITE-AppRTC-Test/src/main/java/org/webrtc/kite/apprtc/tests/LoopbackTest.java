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
package org.webrtc.kite.apprtc.tests;

import org.webrtc.kite.apprtc.checks.PeerConnectionCheck;
import org.webrtc.kite.apprtc.checks.RemoteVideoDisplayCheck;
import org.webrtc.kite.apprtc.steps.JoinRoomStep;
import org.webrtc.kite.stats.GetStatsStep;
import org.webrtc.kite.tests.TestRunner;

import java.util.Random;

public class LoopbackTest extends AppRTCTest {
  final Random rand = new Random(System.currentTimeMillis());
  final String roomId = String.valueOf(Math.abs(rand.nextLong()));
  
  @Override
  protected String debugOption() {
    return "?debug=loopback";
  }
  
  @Override
  public void populateTestSteps(TestRunner runner) {
    JoinRoomStep joinRoomStep = new JoinRoomStep(runner);
    joinRoomStep.setRoomId(roomId);
    joinRoomStep.setDebugOption(this.debugOption() == null ? "" : this.debugOption());
    runner.addStep(joinRoomStep);
    runner.addStep(new PeerConnectionCheck(runner));
    runner.addStep(new RemoteVideoDisplayCheck(runner));
    if (this.getStats()) {
      runner.addStep(new GetStatsStep(runner, getStatsConfig));
    }
  }
}
