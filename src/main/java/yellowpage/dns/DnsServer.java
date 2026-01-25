package yellowpage.dns;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import yellowpage.config.YellowPageConfig;
import yellowpage.udp.AsyncUdpConnector;
import yellowpage.udp.BlockingUdpConnector;
import yellowpage.udp.UdpConnector;

public class DnsServer {

  private final ExecutorService resolverPool;
  private final InboundUdpHandler handler;
  private final UdpConnector udpConnection;
  private final AtomicBoolean running;

  public DnsServer(YellowPageConfig config) {

    // Setup UDP connection
    var bindPort = config.getServerPort();
    var bindIpOpt = config.getServerIp();
    var bindAddr = bindIpOpt.map(ip -> new InetSocketAddress(ip, bindPort)).orElseGet(() -> new InetSocketAddress(bindPort));
    udpConnection = new AsyncUdpConnector(
      new BlockingUdpConnector(bindAddr), 
      512);

    // Setup handler
    var forwardAddr = config.getForwarderAddress();
    this.handler = new InboundUdpHandler(udpConnection, forwardAddr);

    // Start workers
    int poolSize = 10;
    resolverPool = Executors.newFixedThreadPool(poolSize);
    running = new AtomicBoolean(true);
    for(int i=0; i<10; i++){
      resolverPool.submit(this::handlerTask);
    }
  }

  private void handlerTask() {
    var currentThread = Thread.currentThread();
    while(!currentThread.isInterrupted()){
      var message = udpConnection.receive();
      if(message != null)
        handler.handleInboundUdpEvent(message);
    }
  }

  public void stop(){
    running.set(false);
    this.resolverPool.shutdown();
  }
}
