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

package org.webrtc.kite.wpt.dashboard.exception;

/**
 * The KiteUnsupportedRemoteException is thrown if name of the remote is other than the specified
 * ones.
 */
public class KiteUnsupportedRemoteException extends Exception {

  private static final long serialVersionUID = -4432513235311821656L;
  private String remoteName;

  /**
   * Constructs a KiteUnsupportedRemoteException with the specified remote name.
   *
   * @param remoteName name of the unsupported remote
   */
  public KiteUnsupportedRemoteException(String remoteName) {
    super();
    this.remoteName = remoteName;
  }

  public String getRemoteName() {
    return remoteName;
  }

}
