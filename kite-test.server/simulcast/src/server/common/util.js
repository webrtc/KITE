import fs from 'fs'
import crypto from 'crypto'
import { promisify } from 'util'

export const readFile = promisify(fs.readFile)
export const randomBytes = promisify(crypto.randomBytes)

export const randomId = async () => {
  const buffer = await randomBytes(16)
  return buffer.toString('hex')
}

export const readJson = async path => {
  const content = await readFile(path, 'utf8')
  return JSON.parse(content)
}

export const wrapHandler = handler =>
  async (request, response, next) => {
    try {
      const result = await handler(request, response)
      return response.json(result)
    } catch (err) {
      next(err)
    }
  }

export const handleError = () => {
  process.on('unhandledRejection', err => {
    console.error('unhandled rejection:', err.stack)
    process.exit(0)
  })

  process.on('uncaughtException', err => {
    console.error('uncaught exception:', err.stack)
    process.exit(0)
  })
}
