package yellowpage.udp;

import java.net.SocketAddress;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UdpMessage {

  public static UdpMessage inbound(byte[] data, SocketAddress address){
    return new UdpMessage(data, address, System.currentTimeMillis(), UdpMessageType.INBOUND);
  }

  public static UdpMessage reply(byte[] data, UdpMessage original){
    return new UdpMessage(data, original.address, original.durationStartTime, UdpMessageType.REPLY);
  }

  public static UdpMessage outbound(byte[] data, SocketAddress address){
    return new UdpMessage(data, address, System.currentTimeMillis(), UdpMessageType.OUTBOUND);
  }

  public final byte[] data;
  public final SocketAddress address;
  public final long durationStartTime;
  public final UdpMessageType messageType;

  public enum UdpMessageType {
    INBOUND,
    REPLY,
    OUTBOUND,
  }
}