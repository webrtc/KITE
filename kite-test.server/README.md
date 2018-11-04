#Simulcast server

This contains the web client and server for compliance testing on
the Simulcast protocol for WebRTC. It runs on
[Medooze Media Server Node](https://github.com/medooze/media-server-node)
and is based on its [Simulcast demo](https://github.com/medooze/media-server-demo-node).

## Build Instruction

```
cd simulcast
npm install
npm run build-client
MEDIA_SERVER_IP={ServerIP} npm start
```

## Development

For developing the front end client testing, a separate terminal should
be opened running WebPack so that the client source is automatically
rebuilt when the client source code is updated

```bash
npm run dev
```

## Configuration

The media server IP _must_ be set to an accessible IP address from the test
client. An invalid address would cause the ICE connection to fail. Also note
that `127.0.0.1` does _not_ work with Medooze. The server IP can be changed in
two ways:

  - `serverAddress` field in [config/config.json](config/config.json)
  - The `MEDIA_SERVER_IP` environment variable. Note that this overrides
    any value in config.json.

## Simulcast Testing

Once the server is up and running, navigate to `https://{serverAddress}:8080`
to see view the Simulcast test web page. You should see two videos in the first
row, with the original peer connection holding the original source video
and the one echoed from the media server.

The second row of videos are created with seperate peer connections requesting
for different version of the Simulcast streams - original resolution, 1/2
resolution, and 1/4 resolution.

#Other server
This contains the web client and server for compliance testing on:

```
1. Canvas stream to peer connection.
2. Media recorser api.
3. Multi stream between peer connections.
4. RTC peer connection without adapter.js
5. Peer connection rebroadcasting.
6. SDP negotiation with one codec (H264 and VP8 here)
7. RTC peer connection with audio from WebAudio.
```

It runs mostly on a simple signalling server, inspired
by the [peer connection demo](https://webrtc.github.io/samples/src/content/getusermedia/gum/).

## Build Instruction


```
cd wpt_server
npm install
npm start
```



By default server listens to port 8000. To change this, launch the server as follow instead:

```sh
npm start PORT
```

The port argument is not mandatory.

By default server listens in all interfaces, to change this, edit addr object in bin/www. If you want to test on lots of different browser configurations. We recommend using a server with a CA to avoid any problem with safari and edge.

If the test is to be run on a Selenium Grid, the URL for the server should be the IP address of the machine on which it is being run, instead of https://localhost:8085. You can change this by adding a payload object in the config file, as follow:

    {
        "name": "MediaRecorderAPITest",
        "tupleSize": 1,
        "noOfThreads": 3,
        "description": "This test verifies the implementation of Media Recorder API",
        "testImpl": "org.webrtc.kite.wpt.MediaRecorderAPITest",
        "payload": {
            "url": "https://example.com:8000"
        }
    }