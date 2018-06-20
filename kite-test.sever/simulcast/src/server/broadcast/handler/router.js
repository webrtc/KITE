import { Router } from 'express'
import { wrapHandler } from '../../common/util'
import { createBroadcastSinkHandler } from './sink'
import { createBroadcastSourceHandler } from './source'

export const createBroadcastApiRouter = config => {
  const router = Router()

  const createBroadcastSource = createBroadcastSourceHandler(config)
  const createBroadcastSink = createBroadcastSinkHandler(config)

  router.post('/source', wrapHandler((request, response) => {
    const { offer: rawOffer } = request.body

    return createBroadcastSource({ rawOffer })
  }))

  router.post('/sessions/:sessionId/sink', wrapHandler((request, response) => {
    const { offer: rawOffer } = request.body
    const sessionId = request.params.sessionId

    return createBroadcastSink({
      rawOffer,
      sessionId
    })
  }))

  router.use((err, request, response, next) => {
    console.error(err)
    response.status(500).json({
      message: err.message,
      stack: err.stack
    })
  })

  return router
}
