import http from 'http'
import https from 'https'
import { createApp } from './app'
import { loadConfig } from './config/load'
import { handleError } from './common/util'

const runServer = async () => {
  const config = await loadConfig()

  const {
    key,
    cert,
    httpPort,
    httpsPort,
    serverAddress
  } = config

  const app = createApp(config)

  console.log('Medooze media server listening at address', serverAddress)

  const httpsServer = https.createServer({
    key,
    cert
  }, app)

  httpsServer.listen(httpsPort)

  console.log('HTTPS test server listening at', httpsPort)

  const httpServer = http.createServer(app)

  httpServer.listen(httpPort)
  console.log('HTTP test server listening at', httpPort)
}

if (require.main === module) {
  handleError()
  runServer()
}
