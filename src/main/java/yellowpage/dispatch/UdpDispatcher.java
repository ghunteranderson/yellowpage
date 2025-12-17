package yellowpage.dispatch;

import java.util.HexFormat;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yellowpage.dispatch.UdpListener.UdpInbound;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsMessageParser;

@Slf4j
@RequiredArgsConstructor
public class UdpDispatcher implements Consumer<UdpListener.UdpInbound>{

  private static final boolean LOG_HEX = true;

  private final DnsRequestHandler handler;

  @Override
  public void accept(UdpInbound request) {
    // TODO: Better error handling and logging here.
    DnsMessage query;
    DnsMessage answer;
    try {
      if(LOG_HEX)
        System.out.printf("Request:  %s\n", HexFormat.ofDelimiter(" ").formatHex(request.data));
      query = DnsMessageParser.fromBytes(request.data);
    } catch(RuntimeException ex){
      log.warn("Failed to parse request from " + request.source, ex);
      return;
    }

    try {
      answer = handler.handleDnsRequest(query);
      var ansBytes = DnsMessageParser.toBytes(answer);
      if(LOG_HEX)
        System.out.printf("Response: %s\n", HexFormat.ofDelimiter(" ").formatHex(ansBytes));
      request.send(ansBytes); 
    } catch(RuntimeException ex){
      log.error("Failed to respond to request {}", query.getTxId());
      return;
    }
  }
  
}
