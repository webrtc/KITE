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
import { handleTrackEvent } from '../common/track'

export const createEchoStream = async (container, sessionId) => {
  console.log(`creating echo stream for session ${sessionId}`)
  const pc = new RTCPeerConnection()

  handleTrackEvent(pc, container, 'Broadcasted Stream')

  let offer
  if (pc.addTransceiver) {
    pc.addTransceiver('video', {
      direction: 'recvonly'
    })

    offer = await pc.createOffer()
  } else {
    offer = await pc.createOffer({
      offerToReceiveVideo: true
    })
  }

  console.log('offer SDP:', offer.sdp)

  await pc.setLocalDescription(offer)

  const response = await fetch(`/api/broadcast/sessions/${sessionId}/sink`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      offer: offer.sdp
    })
  })

  if (!response.ok) {
    throw new Error(`API return unexpected status ${response.status}: ${await response.text()}`)
  }

  const result = await response.json()
  console.log('Sink API result:', result)

  console.log('answer SDP:', result.answer)

  await pc.setRemoteDescription({
    type: 'answer',
    sdp: result.answer
  })
}
