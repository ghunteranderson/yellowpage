package yellowpage.metrics;

import io.prometheus.metrics.core.metrics.Counter;
import yellowpage.udp.UdpConnector;
import yellowpage.udp.UdpMessage;

public class MetricsUpdConnector implements UdpConnector {

  private final UdpConnector delegate;
  private final Counter sendSuccess;
  private final Counter sendFailed;
  private final Counter recievedSuccess;
  private final Counter recievedFailed;

  public MetricsUpdConnector(UdpConnector delegate){
    this.delegate = delegate;
    this.sendSuccess = Counter.builder()
      .name("yellowpage_udp_sent_message")
      .register();
    this.sendFailed = Counter.builder()
      .name("yellowpage_udp_sent_error")
      .register();

    this.recievedFailed = null;
    this.recievedSuccess = null;
  }

  @Override
  public void send(UdpMessage message) {
    try {
      delegate.send(message);
      sendSuccess.inc();
    } catch(RuntimeException ex){
      sendFailed.inc();
      throw ex;
    }
  }

  @Override
  public UdpMessage receive() {
    try {
      var message = delegate.receive();
      if(message != null)
        recievedSuccess.inc();
      return message;
    } catch(RuntimeException ex){
      recievedFailed.inc();
      throw ex;
    }
  }

  
  
}
