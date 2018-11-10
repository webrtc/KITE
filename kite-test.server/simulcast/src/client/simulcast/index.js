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
