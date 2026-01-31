package yellowpage.dns;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import yellowpage.config.YellowpageConfig;
import yellowpage.exceptions.YellowpageException;
import yellowpage.udp.AsyncUdpConnector;
import yellowpage.udp.UdpConnector;
import yellowpage.udp.UdpConnectorImpl;
import yellowpage.utils.Log;
import yellowpage.utils.TaskRunner;

public class DnsServer implements AutoCloseable {

  private static final AtomicInteger NEXT_THREAD_ID = new AtomicInteger(1);

  private final YellowpageConfig config;
  private boolean started = false;
  private List<Object> resources;

  public DnsServer(YellowpageConfig config) {
    this.config = config;
    this.resources = new ArrayList<>(10);
  }

  private void handlerTask(UdpConnector udpConnection, DnsMessageDispatcher handler) {
    var currentThread = Thread.currentThread();
    while (!currentThread.isInterrupted()) {
      var message = udpConnection.receive();
      if (message != null)
        handler.handleInboundUdpEvent(message);
    }
  }

  public synchronized void start() {
    if (started)
      throw new YellowpageException("DNS server has already been started. It may not be started again.");

    // Setup ancilary task runner
    var taskRunner = new TaskRunner();
    resources.add(taskRunner);

    // Setup UDP connection
    var bindPort = config.getServerPort();
    var bindIp = config.getServerIp();
    Log.info("DNS server listening on " + bindIp.toString() + ":" + bindPort);
    var udpConnector = new AsyncUdpConnector(
        new UdpConnectorImpl(new InetSocketAddress(bindPort)),
        512);
    resources.add(udpConnector);

    // Setup handler
    var forwardAddr = new InetSocketAddress(config.getForwardIp(), config.getForwardPort());
    var handler = new DnsMessageDispatcher(config, udpConnector, forwardAddr, taskRunner);
    resources.add(handler);

    // Start workers
    int poolSize = 10;
    var resolverPool = Executors.newFixedThreadPool(
        poolSize,
        runnable -> new Thread(runnable, "Yellowpage-Resolver-" + NEXT_THREAD_ID.getAndIncrement()));
    resources.add((AutoCloseable)resolverPool::shutdown);
    for (int i = 0; i < 10; i++) {
      resolverPool.submit(() -> handlerTask(udpConnector, handler));
    }

    started = true;
  }

  @Override
  public void close() {
    Log.info("Stopping Yellowpage server.");
    for (var resource : resources) {
      try {
        if(resource instanceof AutoCloseable closeable)
          closeable.close();
      } catch (Exception ex) {
        Log.error("Could not close resource: " + resource.getClass().getName(), ex);
      }
    }
  }
}
