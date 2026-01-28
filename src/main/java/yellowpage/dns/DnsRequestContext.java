package yellowpage.dns;

import java.net.SocketAddress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import yellowpage.exceptions.YellowpageException;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsMessageParser;
import yellowpage.udp.UdpConnector;
import yellowpage.udp.UdpMessage;


@RequiredArgsConstructor
@Getter
public class DnsRequestContext implements AutoCloseable{

  // External Context
  private final UdpConnector connector;
  
  // Request Context
  private final long requestTime;
  private final SocketAddress address;
  private final UdpMessage udpRequest;
  private final DnsMessage dnsRequest;

  // Response Context
  private boolean replySent;
  private boolean replyCheckDisabled;

  public DnsRequestContext(UdpConnector connector, UdpMessage message){
    this.connector = connector;
    this.requestTime = message.durationStartTime;
    this.address = message.address;
    this.udpRequest = message;
    this.dnsRequest = DnsMessageParser.fromBytes(message.data);
  }

  public void reply(DnsMessage message){
    // This may only be called once and the argument may not be null.
    if(message == null)
      throw new YellowpageException("Cannot send null UDP message.");
    else if(replySent)
      throw new YellowpageException("Cannot queue UDP response. Another message has already been queued.");
    
    var response = UdpMessage.reply(DnsMessageParser.toBytes(message), udpRequest);
    connector.send(response);
  }
  
  public DnsMessage getRequest() {
    return this.dnsRequest;
  }

  public void disableReplyCheck() {
    this.replyCheckDisabled = true;
  }

  @Override
  public void close() {
    if(replySent || replyCheckDisabled)
      return;

    var bytes = DnsMessageParser.toBytes(DnsResponseBuilder.serverError(dnsRequest));
    connector.send(UdpMessage.reply(bytes, udpRequest));
  }

  public UdpClient getUdpClient() {
    return this.connector::send;
  }

  public long getRequestTimeMs() {
    return this.requestTime;
  }

  public interface UdpClient {
    void send(UdpMessage message);
  }

  
}