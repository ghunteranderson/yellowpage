package yellowpage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;

import lombok.extern.java.Log;
import yellowpage.config.YellowpageConfig;
import yellowpage.dns.DnsServer;
import yellowpage.metrics.MetricsServer;

@Log
public class Main {
  public static void main(String[] args) throws IOException {
    long start = System.currentTimeMillis();
    try {
        configureLogging();
        log.info("Starting Yellowpage DNS");

        var config = YellowpageConfig.getInstance();
        startMetricsServer(config);
        startDnsServer(config);
        long end = System.currentTimeMillis();
        log.info(() -> "Startup complete in " + (end - start) + "ms");
    } catch(Exception ex){
      log.log(Level.SEVERE, ex, () -> "Yellopage failed to start. See error below.");
      System.exit(1);
    }
  }

  private static void configureLogging(){
    // Configure log format
    var formatKey = "java.util.logging.SimpleFormatter.format";
    if (!System.getProperties().contains(formatKey))
      System.setProperty(formatKey, "[%4$s] %5$s%6$s%n");

    // Flush log handlers on shutdown
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      var logManager = LogManager.getLogManager();
      var loggerNames = logManager.getLoggerNames();
      while (loggerNames.hasMoreElements()) {
        var logger = logManager.getLogger(loggerNames.nextElement());
        for (var handler : logger.getHandlers()) {
          handler.flush();
        }
      }
    }));
  }

  private static void startDnsServer(YellowpageConfig config){
    var server = new DnsServer(config);
    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
  }

  private static void startMetricsServer(YellowpageConfig config){
    var server = new MetricsServer(config);
    Runtime.getRuntime().addShutdownHook(new Thread(server::stop));
  }

}