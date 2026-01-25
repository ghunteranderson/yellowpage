package yellowpage;

import java.io.IOException;

import lombok.extern.java.Log;
import yellowpage.config.YellowPageConfig;
import yellowpage.dns.DnsServer;

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
    var server = new DnsServer(config);

    var currentThread = Thread.currentThread();
    while(!currentThread.isInterrupted());

    server.stop();
    log.info("Shutting down.");
  }
}