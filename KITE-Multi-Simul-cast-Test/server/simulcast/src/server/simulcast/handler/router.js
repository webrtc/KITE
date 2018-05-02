//
// Copyright 2017 Google Inc.
// 
// Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
// 
// https://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software distributed under the License
// is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
// or implied. See the License for the specific language governing permissions and limitations under
// the License.
//
import { Router } from 'express'
import { wrapHandler } from '../../common/util'
import { createSimulcastSinkHandler } from './sink'
import { createSimulcastSourceHandler } from './source'

export const createSimulcastApiRouter = config => {
  const router = Router()

  const createSimulcastSource = createSimulcastSourceHandler(config)
  const createSimulcastSink = createSimulcastSinkHandler(config)

  router.post('/source', wrapHandler((request, response) => {
    const { offer: rawOffer } = request.body

    return createSimulcastSource({ rawOffer })
  }))

  router.post('/sessions/:sessionId/sink', wrapHandler((request, response) => {
    const {
      rid,
      offer: rawOffer,
      track_id: trackId
    } = request.body
    const sessionId = request.params.sessionId

    return createSimulcastSink({
      rid,
      trackId,
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
