package yellowpage.dns;

import java.net.SocketAddress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yellowpage.exceptions.YellowpageException;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsMessageParser;
import yellowpage.udp.UdpMessage;


@RequiredArgsConstructor
@Getter
public class MessageContext {

  private final SocketAddress sourceAddr;
  private final DnsMessage message;
  private UdpMessage response;

  public void send(DnsMessage message, SocketAddress dest){
    // This may only be called once and the argument may not be null.
    if(dest == null)
      throw new YellowpageException("Cannot send to null destination.");
    if(message == null)
      throw new YellowpageException("Cannot send null UDP message.");
    else if(response != null)
      throw new YellowpageException("Cannot queue UDP response. Another message has already been queued.");

    response = new UdpMessage(DnsMessageParser.toBytes(message), dest);
  }

  public void reply(DnsMessage mesasge){
    this.send(mesasge, sourceAddr);
  }

  
}