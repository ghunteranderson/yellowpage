package yellowpage;

import java.util.logging.LogManager;

import lombok.extern.java.Log;
import yellowpage.config.YellowpageConfig;
import yellowpage.dns.DnsServer;

@Log
public class Main {
  public static void main(String[] args) {
    // Configure log format
    var formatKey = "java.util.logging.SimpleFormatter.format";
    if (!System.getProperties().contains(formatKey))
      System.setProperty(formatKey, "[%4$s] %5$s%6$s%n");

    // Launch DNS Server
    var prog = new Main();
    Runtime.getRuntime().addShutdownHook(new Thread(prog::stop));
    prog.start();
  }
  
  private DnsServer server;

  private void start(){
    if(this.server != null)
      return;

    log.info("Starting Yellowpage DNS Server");

    // Fetch configs
    var config = YellowpageConfig.getInstance();
    this.server = new DnsServer(config);
  }
  
  private void stop() {
    if(this.server == null)
      return;

    // Stop server
    this.server.stop();
    log.info("Shutting down.");
    this.server = null;

    // Flush log handlers
    var logManager = LogManager.getLogManager();
    var loggerNames = logManager.getLoggerNames();
    while(loggerNames.hasMoreElements()){
      var logger = logManager.getLogger(loggerNames.nextElement());
      for(var handler : logger.getHandlers()){
        handler.flush();
      }
    }
  }

}