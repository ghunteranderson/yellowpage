package yellowpage.udp;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncUdpConnector implements UdpConnector {

  private static final AtomicInteger NEXT_THREAD_ID = new AtomicInteger(1);

  private Queue<UdpMessage> outbound;
  private Queue<UdpMessage> inbound;

  private Thread worker;

  private UdpConnector udpConnector;

  public AsyncUdpConnector(UdpConnector delegate, int bufferSize){
    this.udpConnector = delegate;
    this.outbound = new ArrayBlockingQueue<>(bufferSize);
    this.inbound = new ArrayBlockingQueue<>(bufferSize);

    this.worker = new Thread(this::run, "Yellowpage-UdpConnector-" + NEXT_THREAD_ID.getAndIncrement());
    this.worker.start();
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
    if(message != null) //empty
      udpConnector.send(message);
  }

  private void recieveOneFromBuffer() throws IOException {
    var message = udpConnector.receive();
    if(message != null) // should always be true
      inbound.add(message); // May throw exception if full
  }

  public void stop(){
    this.worker.interrupt();
  }

  @Override
  public void send(UdpMessage message) {
    this.outbound.add(message);
  }

  @Override
  public UdpMessage receive() {
    return this.inbound.poll();
  }
  
}
