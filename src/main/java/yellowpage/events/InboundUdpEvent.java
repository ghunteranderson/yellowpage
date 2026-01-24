package yellowpage.events;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import yellowpage.udp.UdpConnector.UdpMessage;

@Data
@RequiredArgsConstructor
public class InboundUdpEvent {
  private final UdpMessage udpMessage;
}
