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
