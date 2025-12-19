package yellowpage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import lombok.extern.java.Log;
import yellowpage.config.YellowPageConfig;
import yellowpage.dispatch.UdpDispatcher;
import yellowpage.dispatch.UdpListener;
import yellowpage.dns.DnsResolver;
import yellowpage.repos.ZoneRepoFactory;

@Log
public class Main {
  public static void main(String[] args) throws IOException {
    // Configure log format
    var formatKey = "java.util.logging.SimpleFormatter.format";
    if(!System.getProperties().contains(formatKey))
      System.setProperty(formatKey, "[%4$s] %5$s%n");

    log.info("Starting Yellowpage DNS Server");

    // Fetch configs
    var config = YellowPageConfig.getInstance();
    var port = config.getServerPort();
    var ipOpt = config.getServerIp();

    // Setup DNS request handler
    var repo = ZoneRepoFactory.newInstance();
    var handler = new DnsResolver(repo);
    var dispatcher = new UdpDispatcher(handler);

    // Start UDP listener
    var channel = DatagramChannel.open();
    if(ipOpt.isPresent()){
      var ip = ipOpt.get(); 
      log.info("Starting UDP listener at " + ip.getHostAddress() + ":" + port);
      channel.bind(new InetSocketAddress(ip, port));
    }
    else {
      log.info(() -> "Starting UDP listener at 0.0.0.0:" + port);
      channel.bind(new InetSocketAddress(port));
    }
    channel.configureBlocking(false);
    new UdpListener(channel, dispatcher).start();
    channel.close();

    log.info("Shutting down.");
  }
}