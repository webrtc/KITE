//
// Copyright 2017 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
// 
// https://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and limitations under
// the License.
//
import { forwardStream } from '../streaming/forward'
import { initOffer } from '../../common/streaming/init'

export const createSimulcastSinkHandler = config => {
  const { endpoint, simulcastSessionTable } = config

  return async args => {
    const {
      rid,
      trackId,
      rawOffer,
      sessionId
    } = args

    if (typeof rawOffer !== 'string') {
      throw new Error('offer must be provided as JSON string')
    }

    const streamTable = simulcastSessionTable.get(sessionId)
    if (!streamTable) {
      throw new Error(`invalid session ID ${sessionId}`)
    }

    const trackTable = streamTable.get(trackId)
    if (!trackTable) {
      throw new Error(`invalid track ID ${trackId}`)
    }

    const incomingTrack = trackTable.get(rid)
    if (!incomingTrack) {
      throw new Error(`invalid rid ${rid}`)
    }

    const { transport, offer, answer } = initOffer(endpoint, rawOffer)
    forwardStream(transport, offer, answer, trackId, rid, incomingTrack)

    return {
      answer: answer.toString()
    }
  }
}
