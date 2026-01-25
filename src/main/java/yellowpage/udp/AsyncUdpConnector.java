package yellowpage.udp;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncUdpConnector implements UdpConnector {

  private Queue<UdpMessage> outbound;
  private Queue<UdpMessage> inbound;

  private ExecutorService worker;

  private UdpConnector udpConnector;

  public AsyncUdpConnector(UdpConnector delegate, int bufferSize){
    this.udpConnector = delegate;
    this.outbound = new ArrayBlockingQueue<>(bufferSize);
    this.inbound = new ArrayBlockingQueue<>(bufferSize);

    this.worker = Executors.newFixedThreadPool(1);
    this.worker.submit(this::run);
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
    this.worker.shutdown();
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
