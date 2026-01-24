package yellowpage.dns;

import java.net.SocketAddress;
import java.util.HexFormat;
import java.util.logging.Level;

import lombok.extern.java.Log;
import yellowpage.events.EventBus;
import yellowpage.events.InboundUdpEvent;
import yellowpage.events.OutboundUdpEvent;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsMessageParser;
import yellowpage.repos.ZoneRepo;
import yellowpage.udp.UdpConnector.UdpMessage;

@Log
public class InboundUdpDispatcher {

  private DnsForwarder forwarder;
  private DnsResolver resolver;
  private SocketAddress forwardAddr;

  public InboundUdpDispatcher(ZoneRepo zoneRepo, SocketAddress forwardAddr){
    this.forwardAddr = forwardAddr;
    this.forwarder = new DnsForwarder(forwardAddr);
    this.resolver = new DnsResolver(zoneRepo, forwarder);
  }

  public void handleInboundUdpEvent(EventBus bus, InboundUdpEvent event){

    var udpMessageIn = event.getUdpMessage();
    var sourceAddr = udpMessageIn.address;
    var isForwardResponse = forwardAddr.equals(sourceAddr);

    // Attempt to parse message
    DnsMessage dnsMessageIn;
    try {
      dnsMessageIn = DnsMessageParser.fromBytes(udpMessageIn.data);
    } catch(Exception ex){
      Level level = isForwardResponse ? Level.SEVERE : Level.FINE;
      log.log(level, ex, () -> "Failed to parse DNS message: " + HexFormat.ofDelimiter(" ").formatHex(udpMessageIn.data));
      return;
    }

    // Dispatch based based on who sent the message
    DnsMessageHandler handler;
    if(isForwardResponse){
      handler = forwarder;
    }
    else {
      handler = resolver; 
    }

    UdpMessage udpMessageOut = null;
    try {
      // Attempt to get DNS message from handler
      var msgContext = new MessageContext(sourceAddr, dnsMessageIn);
      handler.handleInboundDnsMessage(msgContext);
      udpMessageOut = msgContext.getResponse();
      if(udpMessageOut == null)
        log.log(Level.SEVERE, () -> "No response or error was generated for " + sourceAddr);
    } catch (Exception ex){
      // Fall back to error message
      log.log(Level.WARNING, ex, () -> "Failed to handle request from " + sourceAddr);
    }

    // Queue Resposne
    if(udpMessageOut != null){
      bus.fire(new OutboundUdpEvent(udpMessageOut));
    }
    // Handle error cases
    else if(!isForwardResponse){
      // Do not send error to upstream DNS
      bus.fire(new UdpMessage(
        DnsMessageParser.toBytes(DnsResponseBuilder.serverError(dnsMessageIn)),
        sourceAddr
      ));
    }

  }

}


