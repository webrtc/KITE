// import { createRecorder } from 'medooze-media-server'
import {
  videoCodecs,
  headerExtensions
} from '../../common/streaming/param'

/*
  Accept a simulcast stream by:
    1. Take a transport and its associated offer / answer
    2. Create IncomingStream object for each rid stream and
       store them in a stream table
    3. Add the accepted video offer to answer
    4. Return the stream table

  acceptSimulcastStream ::
    Transport
    -> SDPInfo
    -> SDPInfo
    -> Map String (Map String (Map String IncomingStreamTrack))
 */
export const acceptSimulcastStream = (transport, offer, answer) => {
  // videoOffer :: MediaInfo
  const videoOffer = offer.getMedia('video')
  if (!videoOffer) {
    throw new Error('offer must contain simulcast video stream')
  }

  // simulcastInfo :: Optional SimulcastInfo
  const simulcastInfo = videoOffer.getSimulcast()
  if (!simulcastInfo) {
    // console.warn('offer do not support simulcast')
    throw new Error('offer video must have simulcast enabled')
  }

  // videoAnswer :: MediaInfo
  const videoAnswer = videoOffer.answer({
    codecs: videoCodecs,
    extensions: headerExtensions,
    simulcast: true
  })

  answer.addMedia(videoAnswer)

  transport.setLocalProperties({
    video: answer.getMedia('video')
  })

  // streamTable :: Map String (Map String IncomingStreamTrack)
  const streamTable = new Map()

  // offerStreams :: Map String StreamInfo
  const offerStreams = offer.getStreams()

  // streamInfo :: StreamInfo
  for (const streamInfo of offerStreams.values()) {
    // incomingStream :: IncomingStream
    const incomingStream = transport.createIncomingStream(streamInfo)

    const outgoingStream = transport.createOutgoingStream({
      video: true
    })

    outgoingStream.attachTo(incomingStream)
    answer.addStream(outgoingStream.getStreamInfo())

    // trackInfos :: Map String TrackInfo
    const trackInfos = streamInfo.getTracks()

    // incomingTracks:: Array IncomingStreamTrack
    const incomingTracks = incomingStream.getTracks()

    for (const incomingTrack of incomingTracks) {
      const trackId = incomingTrack.getId()
      const trackInfo = trackInfos.get(trackId)

      // trackTable :: Map String IncomingStreamTrack
      const trackTable = new Map()
      streamTable.set(trackId, trackTable)

      // encodings :: Array (Array TrackEncodingInfo)
      const encodings = trackInfo.getEncodings()

      for (const alternatives of encodings) {
        for (const encodingInfo of alternatives) {
          const rid = encodingInfo.getId()
          trackTable.set(rid, incomingTrack)
        }
      }
    }
  }

  return streamTable
}
