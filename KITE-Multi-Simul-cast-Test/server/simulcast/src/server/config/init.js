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
import { readFile } from '../common/util'
import MediaServer from 'medooze-media-server'

export const initConfig = async rawConfig => {
  const {
    debug,
    keyPath,
    certPath,
    httpPort,
    httpsPort,
    staticDir,
    serverAddress
  } = rawConfig

  const key = await readFile(keyPath)
  const cert = await readFile(certPath)

  const endpoint = MediaServer.createEndpoint(serverAddress)

  const simulcastSessionTable = new Map()
  const broadcastSessionTable = new Map()

  if (debug) {
    MediaServer.enableDebug(true)
  }

  return {
    key,
    cert,
    endpoint,
    httpPort,
    httpsPort,
    staticDir,
    serverAddress,
    simulcastSessionTable,
    broadcastSessionTable
  }
}
