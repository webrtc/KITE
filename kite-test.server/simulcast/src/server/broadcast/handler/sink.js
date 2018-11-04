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
