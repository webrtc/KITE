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
import { randomId } from '../../common/util'
import { initOffer } from '../../common/streaming/init'
import { acceptBroadcastStream } from '../streaming/accept'

export const createBroadcastSourceHandler = config => {
  const { endpoint, broadcastSessionTable } = config

  return async args => {
    const { rawOffer } = args

    if (typeof rawOffer !== 'string') {
      throw new Error('offer must be provided as JSON string')
    }

    const { transport, offer, answer } = initOffer(endpoint, rawOffer)
    const incomingStream = acceptBroadcastStream(transport, offer, answer)

    const sessionId = await randomId()

    broadcastSessionTable.set(sessionId, incomingStream)

    transport.on('stopped', ev => {
      console.log(`source for session ${sessionId} has stopped`)
      broadcastSessionTable.delete(sessionId)
    })

    return {
      session_id: sessionId,
      answer: answer.toString()
    }
  }
}
