import express from 'express'
import bodyParser from 'body-parser'
import { createSimulcastApiRouter } from './simulcast/handler/router'
import { createBroadcastApiRouter } from './broadcast/handler/router'

export const createApp = config => {
  const { staticDir } = config

  const app = express()

  app.use(bodyParser.json())
  app.use('/api/simulcast', createSimulcastApiRouter(config))
  app.use('/api/broadcast', createBroadcastApiRouter(config))
  app.use(express.static(staticDir))

  return app
}
