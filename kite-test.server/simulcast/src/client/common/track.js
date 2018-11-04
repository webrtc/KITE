export const handleTrackEvent = (pc, container, label) => {
  pc.addEventListener('track', ev => {
    console.log('ontrack event:', ev)

    const videoTrack = ev.track
    const videoElement = document.createElement('video')
    videoElement.srcObject = new MediaStream([videoTrack])
    videoElement.autoplay = true

    const desc = document.createElement('h2')
    desc.innerText = label

    const idField = document.createElement('h4')
    idField.innerText = `track ID: ${videoTrack.id}`

    const div = document.createElement('div')
    div.classList.add('video-container')
    div.appendChild(videoElement)
    div.appendChild(desc)
    div.appendChild(idField)

    container.appendChild(div)
  })
}
