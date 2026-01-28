package yellowpage.dns;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.java.Log;
import yellowpage.config.YellowpageConfig;
import yellowpage.udp.AsyncUdpConnector;
import yellowpage.udp.UdpConnectorImpl;
import yellowpage.udp.UdpConnector;

@Log
public class DnsServer {

  private static final AtomicInteger NEXT_THREAD_ID = new AtomicInteger(1);

  private final ExecutorService resolverPool;
  private final DnsMessageDispatcher handler;
  private final UdpConnector udpConnection;
  private final AtomicBoolean running;

  public DnsServer(YellowpageConfig config) {
    // Setup UDP connection
    var bindPort = config.getServerPort();
    var bindIpOpt = config.getServerIp();
    var bindAddr = bindIpOpt.map(ip -> new InetSocketAddress(ip, bindPort)).orElseGet(() -> new InetSocketAddress(bindPort));
    log.info("DNS server listening on " + bindAddr.getHostString() + ":" + bindAddr.getPort());
    udpConnection = new AsyncUdpConnector(
      new UdpConnectorImpl(bindAddr), 
      512);

    // Setup handler
    var forwardAddr = config.getForwarderAddress();
    log.info("Forwarding address set to " + forwardAddr.getHostString() + ":" + forwardAddr.getPort());
    this.handler = new DnsMessageDispatcher(config, udpConnection, forwardAddr);

    // Start workers
    int poolSize = 10;
    resolverPool = Executors.newFixedThreadPool(
      poolSize, 
      runnable -> new Thread(runnable, "Yellowpage-Resolver-" + NEXT_THREAD_ID.getAndIncrement()));
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
