import {
  videoCodecs
} from '../../common/streaming/param'

export const acceptBroadcastStream = (transport, offer, answer) => {
  // videoOffer :: MediaInfo
  const videoOffer = offer.getMedia('video')
  if (!videoOffer) {
    throw new Error('offer must contain simulcast video stream')
  }

  // videoAnswer :: MediaInfo
  const videoAnswer = videoOffer.answer({
    codecs: videoCodecs
  })

  answer.addMedia(videoAnswer)

  transport.setLocalProperties({
    video: answer.getMedia('video')
  })

  const [streamInfo] = offer.getStreams().values()
  if (!streamInfo) {
    throw new Error('expect at least one stream available in offer')
  }

  console.log('streamInfo:', streamInfo)

  // incomingStream :: IncomingStream
  const incomingStream = transport.createIncomingStream(streamInfo)

  const outgoingStream = transport.createOutgoingStream({
    video: true
  })

  outgoingStream.attachTo(incomingStream)
  answer.addStream(outgoingStream.getStreamInfo())

  return incomingStream
}
