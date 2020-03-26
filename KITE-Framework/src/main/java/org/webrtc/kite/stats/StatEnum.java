package org.webrtc.kite.stats;

public enum StatEnum {
  JITTER("Audio Jitter (ms)"),
  AVG_JITTER("Average Audio Jitter (ms)"),
  
  AUDIO_LEVEL("Audio Level (dB)"),
  AVG_AUDIO_LEVEL("Average Audio Level (dB)"),
  
  FRAME("Frames"),
  TOTAL_FRAME_RECEIVED("Frames Received"),
  TOTAL_FRAME_SENT("Frames Sent"),
  FRAME_DECODED("Frames Decoded"),
  FRAME_CORRUPTED("Frames Corrupted"),
  FRAME_DROPPED("Frames Dropped"),
  FRAME_RATE("Frame Rate (fps)"),
  AVG_FRAME_RATE("Average Frame Rate (fps)"),
  AVG_FRAME_RATE_DECODED("Average Frame Rate Decoded (fps)"),
  
  BYTES("Bytes"),
  TOTAL_BYTES_SENT("Total Bytes Sent (Bytes)"),
  TOTAL_INBOUND_BYTES_RECEIVED("Total Inbound Bytes Received (Bytes)"),
  TOTAL_INBOUND_AUDIO_BYTES_RECEIVED("Total Inbound Audio Bytes Received (Bytes)"),
  TOTAL_INBOUND_VIDEO_BYTES_RECEIVED("Total Inbound Video Bytes Received (Bytes)"),
  TOTAL_OUTBOUND_BYTES_SENT("Total Outbound Bytes Sent (Bytes)"),
  TOTAL_OUTBOUND_AUDIO_BYTES_SENT("Total Outbound Audio Bytes Sent (Bytes)"),
  TOTAL_OUTBOUND_VIDEO_BYTES_SENT("Total Outbound Video Bytes Sent (Bytes)"),
  TOTAL_BYTES_RECEIVED("Total Bytes Received (Bytes)"),
  
  SENT_BITRATE("Average Sent Bitrate (kbps)"),
  RECEIVED_BITRATE("Average Received Bitrate (kbps)"),
  
  PACKETS("Packets"),
  PACKETS_LOST("Packets Lost"),
  PACKETS_LOST_PERCENTAGE("Packets Lost (%)"),
  PACKETS_LOST_CUMULATIVE_PERCENTAGE("Cumulative Packets Lost (%)"),
  PACKETS_DISCARDED("Packets Discarded"),
  PACKETS_DISCARDED_PERCENTAGE("Packets Discarded (%)"),
  TOTAL_PACKETS_SENT("Total Packets Sent"),
  TOTAL_PACKETS_RECEIVED("Total Packets Received"),
  TOTAL_PACKETS_LOST("Total Packets Lost"),
  TOTAL_PACKETS_DISCARDED("Total Packets Lost"),
  AVG_PACKETS_RECEIVED("Average Packets Received"),
  AVG_PACKETS_SENT("Average Packets Sent"),
  AVG_PACKETS_LOST("Average Packets Lost"),
  
  CURRENT_RTT("Current Round Trip Time (ms)"),
  AVG_CURRENT_RTT("Average Current Round Trip Time (ms)"),
  TOTAL_RTT("Total Round Trip Time (ms)"),
  
  REMOTE_IP("Remote IP")
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
