import { loadConfig } from '../server/config/load'
import { readJson, handleError } from '../server/common/util'
import { createSimulcastSourceHandler } from '../server/simulcast/handler/source'
import { createSimulcastSinkHandler } from '../server/simulcast/handler/sink'

const runTest = async () => {
  const config = await loadConfig()

  const createSimulcastSource = createSimulcastSourceHandler(config)
  const createSimulcastSink = createSimulcastSinkHandler(config)

  const { offer: offer1 } = await readJson('fixture/offer-1.json')
  const { offer: offer2 } = await readJson('fixture/offer-2.json')

  const sourceResult = await createSimulcastSource({ rawOffer: offer1 })
  console.log('source result:', sourceResult)

  const sessionId = sourceResult.session_id
  for (const [trackId, rids] of Object.entries(sourceResult.tracks)) {
    for (const rid of rids) {
      const sinkResult = await createSimulcastSink({
        rawOffer: offer2,
        rid,
        trackId,
        sessionId
      })

      console.log(`sink result for track ${trackId} rid ${rid}:`, sinkResult)
    }
  }
}

handleError()

runTest()
  .then(() => process.exit(0))
