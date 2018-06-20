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

export const streamSimulcast = async (sinkVideoContainer, videoElement) => {
  const mediaStream = await navigator.mediaDevices.getUserMedia({
    video: true
  })
  console.log('mediaStream:', mediaStream)

  videoElement.srcObject = mediaStream
  videoElement.autoplay = true

  const pc = new RTCPeerConnection()

  handleTrackEvent(pc, sinkVideoContainer, 'Echo Stream from same PC')

  const videoTrack = mediaStream.getVideoTracks()[0]
  console.log('video track:', videoTrack)
  if (!videoTrack) {
    throw new Error('expect at least video track to be present')
  }

  const transceiver = pc.addTransceiver(videoTrack, {
    direction: 'sendrecv',
    sendEncodings: [
      { rid: 'original' },
      { rid: 'half', scaleDownResolutionBy: 2.0 },
      { rid: 'quarter', scaleDownResolutionBy: 4.0 }
    ]
  })

  // const sender = pc.addTrack(videoTrack)

  // TODO: Remove setParameters call when releasing.
  // rid is a read only parameter and should not
  // be modified by setParameters. The correct way
  // to enable simulcast should be by setting the
  // same parameters in addTransceiver, but that
  // is currently not working in Firefox.
  /*await transceiver.sender.setParameters({
    encodings: [
      { rid: 'original' },
      { rid: 'half', scaleDownResolutionBy: 2.0 },
      { rid: 'quarter', scaleDownResolutionBy: 4.0 }
    ]
  })*/

  console.log('transceiver after set param:', transceiver)

  const offer = await pc.createOffer()
  await pc.setLocalDescription(offer)

  console.log('offer SDP:', offer.sdp)

  // TODO: Remove the format conversion when releasing.
  // Firefox is using an older format of the simulcast line.
  // We convert it to the new format temporarily to test the test.
  //const offerSdp = offer.sdp.replace(': send rid=', ':send ')
  const offerSdp = offer.sdp

  const response = await fetch('/api/simulcast/source', {
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
  const { tracks } = result

  // TODO: Remove the format conversion when releasing.
  //const answerSdp = result.answer.replace(':recv ', ': recv rid=')
  const answerSdp = result.answer

  console.log('answer SDP:', answerSdp)

  await pc.setRemoteDescription({
    type: 'answer',
    sdp: answerSdp
  })

  return {
    tracks,
    sessionId,
    pc
  }
}
