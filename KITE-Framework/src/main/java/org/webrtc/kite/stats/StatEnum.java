package org.webrtc.kite.stats;

public enum StatEnum {
  JITTER("audio_jitter(ms)"),
  AVG_JITTER("avg_audio_jitter(ms)"),
  
  AUDIO_LEVEL("audio_level(dB)"),
  AVG_AUDIO_LEVEL("avg_audio_level(dB)"),
  
  FRAME("frames"),
  TOTAL_FRAME_RECEIVED("frames_rcv"),
  TOTAL_FRAME_SENT("frames_sent"),
  FRAME_DECODED("frames_decoded"),
  FRAME_CORRUPTED("frames_corrupted"),
  FRAME_DROPPED("frames_dropped"),
  FRAME_RATE("frame_rate(fps)"),
  AVG_FRAME_RATE("avg_frame_rate(fps)"),
  AVG_FRAME_RATE_DECODED("avg_frame_decoded_rate(fps)"),
  
  BYTES("bytes"),
  TOTAL_BYTES_SENT("total_bytes_sent(bytes)"),
  TOTAL_INBOUND_BYTES_RECEIVED("total_inbound_bytes_rcv(bytes)"),
  TOTAL_INBOUND_AUDIO_BYTES_RECEIVED("total_inbound_audio_bytes_rcv(bytes)"),
  TOTAL_INBOUND_VIDEO_BYTES_RECEIVED("total_inbound_video_bytes_rcv(bytes)"),
  TOTAL_OUTBOUND_BYTES_SENT("total_outbound bytes_sent(bytes)"),
  TOTAL_OUTBOUND_AUDIO_BYTES_SENT("total_outbound_audio_bytes_sent(bytes)"),
  TOTAL_OUTBOUND_VIDEO_BYTES_SENT("total_outbound_video_bytes_sent(bytes)"),
  TOTAL_BYTES_RECEIVED("total_bytes_rcv(bytes)"),
  
  SENT_BITRATE("avg_sent_bitrate(kbps)"),
  RECEIVED_BITRATE("avg_rcv_bitrate(kbps)"),
  
  PACKETS("packets"),
  PACKETS_LOST("packets_Lost"),
  PACKETS_LOST_PERCENTAGE("packets_lost(%)"),
  PACKETS_LOST_CUMULATIVE_PERCENTAGE("cumulative_packets_lost(%)"),
  PACKETS_DISCARDED("packets_discarded"),
  PACKETS_DISCARDED_PERCENTAGE("packets_discarded(%)"),
  TOTAL_PACKETS_SENT("total_packets_sent"),
  TOTAL_PACKETS_RECEIVED("total_packets_rcv"),
  TOTAL_PACKETS_LOST("total_packets_lost"),
  TOTAL_PACKETS_DISCARDED("total_packets_lost"),
  AVG_PACKETS_RECEIVED("avg_packets_rcv"),
  AVG_PACKETS_SENT("avg_packets_sent"),
  AVG_PACKETS_LOST("avg_packets_lost"),
  
  CURRENT_RTT("current_rtt(ms)"),
  AVG_CURRENT_RTT("avg_current_rtt(ms)"),
  TOTAL_RTT("total_rtt(ms)"),
  
  REMOTE_IP("remote_IP")
  ;
  
  private final String text;
  
  /**
   * @param text
   */
  StatEnum(final String text) {
    this.text = text;
  }
  
  /* (non-Javadoc)
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return text;
  }
}
