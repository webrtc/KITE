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
