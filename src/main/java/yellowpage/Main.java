package yellowpage;

import java.io.IOException;
import java.net.InetSocketAddress;

import lombok.extern.java.Log;
import yellowpage.config.YellowPageConfig;
import yellowpage.dns.InboundUdpDispatcher;
import yellowpage.events.OutboundUdpEvent;
import yellowpage.events.InboundUdpEvent;
import yellowpage.events.EventBus;
import yellowpage.repos.ZoneRepoFactory;
import yellowpage.udp.UdpConnector;

@Log
public class Main {
  public static void main(String[] args) throws IOException {

    // Configure log format
    var formatKey = "java.util.logging.SimpleFormatter.format";
    if (!System.getProperties().contains(formatKey))
      System.setProperty(formatKey, "[%4$s] %5$s%6$s%n");

    log.info("Starting Yellowpage DNS Server");

    // Fetch configs
    var config = YellowPageConfig.getInstance();
    var port = config.getServerPort();
    var ipOpt = config.getServerIp();

    // Setup DNS request handler
    var bus = new EventBus();
    var repo = ZoneRepoFactory.newInstance();

    var resolver = new InboundUdpDispatcher(repo, config.getForwarderAddress());
    bus.register(InboundUdpEvent.class, resolver::handleInboundUdpEvent);

    // Start UDP listener
    var address = ipOpt.map(ip -> new InetSocketAddress(ip, port)).orElseGet(() -> new InetSocketAddress(port));
    var listener = new UdpConnector(address);
    listener.onRecieve(message -> bus.fire(new InboundUdpEvent(message)));
    bus.register(OutboundUdpEvent.class, (b, event) -> listener.send(event.getUdpMessage()));
    listener.start(); // Blocks. Main thread becomes the listener.

    log.info("Shutting down.");
  }
}