import { streamBroadcast } from './source'
import { createEchoStream } from './sink'

export const runBroadcastApp = async () => {
  console.log('running broadcast test')

  const sourceVideo = document.getElementById('source-video')
  const sourceVideoContainer = document.getElementById('source-videos')
  const sinkVideoContainer = document.getElementById('sink-videos')

  const result = await streamBroadcast(sourceVideoContainer, sourceVideo)
  console.log('stream result:', result)

  const { sessionId } = result
  await createEchoStream(sinkVideoContainer, sessionId)
}

window.addEventListener('load', () =>
  runBroadcastApp().catch(console.error))
