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
