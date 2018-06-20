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

export const streamBroadcast = async (sinkVideoContainer, videoElement) => {
  const mediaStream = await navigator.mediaDevices.getUserMedia({
    video: true
  })
  console.log('mediaStream:', mediaStream)

  videoElement.srcObject = mediaStream
  videoElement.autoplay = true

  const pc = new RTCPeerConnection()

  handleTrackEvent(pc, sinkVideoContainer, 'Echo Stream')

  const videoTrack = mediaStream.getVideoTracks()[0]
  console.log('video track:', videoTrack)
  if (!videoTrack) {
    throw new Error('expect at least video track to be present')
  }

  pc.addTrack(videoTrack, mediaStream)

  const offer = await pc.createOffer()
  await pc.setLocalDescription(offer)

  const offerSdp = offer.sdp
  console.log('offer SDP:', offerSdp)

  const response = await fetch('/api/broadcast/source', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      offer: offerSdp
    })
  })

  if (!response.ok) {
    throw new Error(`API return unexpected status ${response.status}: ${await response.text()}`)
  }

  const result = await response.json()
  console.log('API result:', result)

  const sessionId = result.session_id
  const answerSdp = result.answer

  console.log('answer SDP:', answerSdp)

  await pc.setRemoteDescription({
    type: 'answer',
    sdp: answerSdp
  })

  return {
    sessionId
  }
}
