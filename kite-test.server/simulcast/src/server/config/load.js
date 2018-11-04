import { initConfig } from './init'
import { readJson } from '../common/util'

export const loadConfig = async () => {
  const rawConfig = await readJson('config/config.json')

  const serverAddress = process.env.MEDIA_SERVER_IP
  if (serverAddress) {
    rawConfig.serverAddress = serverAddress
  }

  return initConfig(rawConfig)
}
