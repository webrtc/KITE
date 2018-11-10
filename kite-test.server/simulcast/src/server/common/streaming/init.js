import { SDPInfo } from 'semantic-sdp'

/*
  Initialize an offer using an endpoint with
  parsed offer SDPInfo, underlying transport,
  and initial answer SDPInfo with DTLS and ICE
  setup done.

  initOffer ::
    EndPoint
    -> SDPInfo
    -> {  offer :: SDPInfo,
          answer :: SDPInfo,
          transport :: Transport  }
 */
export const initOffer = (endpoint, offerSdp) => {
  const offer = SDPInfo.process(offerSdp)

  // transport :: Transport
  const transport = endpoint.createTransport({
    dtls: offer.getDTLS(),
    ice: offer.getICE()
  })

  transport.setRemoteProperties({
    audio: offer.getMedia('audio'),
    video: offer.getMedia('video')
  })

  // answer :: SDPInfo
  const answer = new SDPInfo()

  // dtlsInfo :: DTLSInfo
  const dtlsInfo = transport.getLocalDTLSInfo()
  answer.setDTLS(dtlsInfo)

  // iceInfo :: ICEInfo
  const iceInfo = transport.getLocalICEInfo()
  answer.setICE(iceInfo)

  // localCandidates :: Array CandidateInfo
  const localCandidates = endpoint.getLocalCandidates()
  for (const candidate of localCandidates) {
    answer.addCandidate(candidate)
  }

  return {
    offer,
    answer,
    transport
  }
}
