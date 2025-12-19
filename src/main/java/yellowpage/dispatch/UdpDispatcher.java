package yellowpage.dispatch;

import java.util.function.Consumer;
import java.util.logging.Level;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import yellowpage.dispatch.UdpListener.UdpInbound;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsMessageParser;

@Log
@RequiredArgsConstructor
public class UdpDispatcher implements Consumer<UdpListener.UdpInbound>{

  private final DnsRequestHandler handler;

  @Override
  public void accept(UdpInbound request) {
    DnsMessage query;
    DnsMessage answer;
    try {
      query = DnsMessageParser.fromBytes(request.data);
    } catch(RuntimeException ex){
      log.log(Level.WARNING, ex, () -> "Failed to parse request from " + request.source);
      return;
    }

    try {
      answer = handler.handleDnsRequest(query);
      var ansBytes = DnsMessageParser.toBytes(answer);
      request.send(ansBytes); 
    } catch(RuntimeException ex){
      log.severe(() -> "Failed to respond to request " + query.getTxId());
      return;
    }
  }
  
}
