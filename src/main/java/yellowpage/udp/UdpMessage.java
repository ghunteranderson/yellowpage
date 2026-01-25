package yellowpage.udp;

import java.net.SocketAddress;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UdpMessage {
  public final byte[] data;
  public final SocketAddress address;
}