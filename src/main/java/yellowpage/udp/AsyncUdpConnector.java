package yellowpage.udp;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import io.prometheus.metrics.core.datapoints.GaugeDataPoint;
import io.prometheus.metrics.core.metrics.Gauge;
import yellowpage.metrics.Metrics;

public class AsyncUdpConnector implements UdpConnector {

  private static final AtomicInteger NEXT_THREAD_ID = new AtomicInteger(1);

  private final Queue<UdpMessage> outbound;
  private final Queue<UdpMessage> inbound;
  private final Thread worker;
  private final UdpConnector udpConnector;

  private final GaugeDataPoint inboundGauge;
  private final GaugeDataPoint outboundGauge;
  

  public AsyncUdpConnector(UdpConnector delegate, int bufferSize){
    this.udpConnector = delegate;
    this.outbound = new ArrayBlockingQueue<>(bufferSize);
    this.inbound = new ArrayBlockingQueue<>(bufferSize);

    this.worker = new Thread(this::run, "Yellowpage-UdpConnector-" + NEXT_THREAD_ID.getAndIncrement());
    this.worker.start();

    this.inboundGauge = Metrics.getUdpBufferedInbound();
    this.outboundGauge = Metrics.getUdpBufferedOutbound();
  }

  private void run(){
    var currentThread = Thread.currentThread();
    while(!currentThread.isInterrupted()){
      try {
        sendOneFromBuffer();
      } catch(Exception ex){
        // TODO: Count error in metrics
      }
      try {
        recieveOneFromBuffer();
      } catch(Exception ex){
        // TODO COunt error in mertircs
      }
    }
  }

  private void sendOneFromBuffer() throws IOException {
    var message = outbound.poll();
    if(message != null){
      udpConnector.send(message);
      outboundGauge.dec();
    }
  }

  private void recieveOneFromBuffer() throws IOException {
    var message = udpConnector.receive();
    if(message != null){  // should always be true
      inbound.add(message); // May throw exception if full
      inboundGauge.inc();
    }
  }

  public void stop(){
    this.worker.interrupt();
  }

  @Override
  public void send(UdpMessage message) {
    var added = this.outbound.add(message);
    if(added)
      this.outboundGauge.inc();
  }

  @Override
  public UdpMessage receive() {
    var message = this.inbound.poll();
    if(message != null)
      this.inboundGauge.dec();
    return message;
  }
  
}
