import { randomId } from '../../common/util'
import { initOffer } from '../../common/streaming/init'
import { acceptSimulcastStream } from '../streaming/accept'

export const createSimulcastSourceHandler = config => {
  const { endpoint, simulcastSessionTable } = config

  return async args => {
    const { rawOffer } = args

    if (typeof rawOffer !== 'string') {
      throw new Error('offer must be provided as JSON string')
    }

    const { transport, offer, answer } = initOffer(endpoint, rawOffer)
    const streamTable = acceptSimulcastStream(transport, offer, answer)

    const sessionId = await randomId()

    simulcastSessionTable.set(sessionId, streamTable)

    transport.on('stopped', ev => {
      console.log(`source for session ${sessionId} has stopped`)
      simulcastSessionTable.delete(sessionId)
    })

    const trackResult = {}
    for (const [trackId, trackTable] of streamTable.entries()) {
      trackResult[trackId] = [...trackTable.keys()]
    }

    return {
      session_id: sessionId,
      answer: answer.toString(),
      tracks: trackResult
    }
  }
}
