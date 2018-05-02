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

export const createBroadcastSinkHandler = config => {
  const { endpoint, broadcastSessionTable } = config

  return async args => {
    const {
      rawOffer,
      sessionId
    } = args

    if (typeof rawOffer !== 'string') {
      throw new Error('offer must be provided as JSON string')
    }

    const incomingStream = broadcastSessionTable.get(sessionId)
    if (!incomingStream) {
      throw new Error(`invalid session ID ${sessionId}`)
    }

    const { transport, offer, answer } = initOffer(endpoint, rawOffer)
    forwardStream(transport, offer, answer, incomingStream)

    return {
      answer: answer.toString()
    }
  }
}
