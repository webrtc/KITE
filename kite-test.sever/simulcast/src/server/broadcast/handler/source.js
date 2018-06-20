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
