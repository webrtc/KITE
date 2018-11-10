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
