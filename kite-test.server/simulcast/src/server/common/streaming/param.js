import {
  CodecInfo
} from 'semantic-sdp'

export const videoCodecs = CodecInfo.MapFromNames(['VP8'], true)

export const headerExtensions = new Set([
  'urn:ietf:params:rtp-hdrext:toffset',
  'http://www.webrtc.org/experiments/rtp-hdrext/abs-send-time',
  'urn:3gpp:video-orientation',
  'http://www.ietf.org/id/draft-holmer-rmcat-transport-wide-cc-extensions-01',
  'http://www.webrtc.org/experiments/rtp-hdrext/playout-delay',
  'urn:ietf:params:rtp-hdrext:sdes:rtp-stream-id',
  'urn:ietf:params:rtp-hdrext:sdes:repair-rtp-stream-id',
  'urn:ietf:params:rtp-hdrext:sdes:mid'
])
