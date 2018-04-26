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
