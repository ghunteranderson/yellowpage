package yellowpage.dns;

import java.net.SocketAddress;

import yellowpage.config.YellowpageConfig;
import yellowpage.udp.UdpConnector;
import yellowpage.udp.UdpMessage;
import yellowpage.utils.Log;
import yellowpage.utils.TaskRunner;

public class DnsMessageDispatcher {

  private final DnsForwarder forwarder;
  private final DnsResolver resolver;
  private final SocketAddress forwardAddr;
  private final UdpConnector udpConnector;

  public DnsMessageDispatcher(YellowpageConfig config, UdpConnector udpConnector, SocketAddress forwardAddr, TaskRunner taskRunner){
    this.udpConnector = udpConnector;
    this.forwardAddr = forwardAddr;
    this.forwarder = new DnsForwarder(forwardAddr);
    this.resolver = new DnsResolver(config, forwarder, taskRunner);
  }

  public void handleInboundUdpEvent(UdpMessage udpMessage){

    var sourceAddr = udpMessage.address;
    var isForwardResponse = forwardAddr.equals(sourceAddr);

    // Dispatch based based on who sent the message
    try (var ctx = new DnsRequestContext(udpConnector, udpMessage)){

      if(isForwardResponse){
        ctx.disableReplyCheck();
        forwarder.handleUpstreamResponse(ctx);
      }
      else {
        resolver.resolve(ctx);
      }
    
    } catch (Exception ex){
      // Fall back to error message
      Log.warn("Failed to handle request from " + sourceAddr, ex);
    }
  }

}


