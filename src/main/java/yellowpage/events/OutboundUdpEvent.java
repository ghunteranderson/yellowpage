package yellowpage.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import yellowpage.udp.UdpConnector.UdpMessage;

@Data
@RequiredArgsConstructor
public class OutboundUdpEvent {
  private final UdpMessage udpMessage;
}
