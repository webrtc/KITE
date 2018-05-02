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
import { streamSimulcast } from './source'
import { createEchoStreams } from './sink'

export const runSimulcastApp = async () => {
  console.log('running simulcast test')

  const sourceVideo = document.getElementById('source-video')
  const sourceVideoContainer = document.getElementById('source-videos')
  const sinkVideoContainer = document.getElementById('sink-videos')

  const result = await streamSimulcast(sourceVideoContainer, sourceVideo)
  console.log('stream result:', result)
  window.simulcast = result
  const { sessionId, tracks } = result
  await createEchoStreams(sinkVideoContainer, sessionId, tracks)
}

window.addEventListener('load', () =>
  runSimulcastApp().catch(console.error))
