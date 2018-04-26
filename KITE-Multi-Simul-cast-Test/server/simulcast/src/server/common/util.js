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
