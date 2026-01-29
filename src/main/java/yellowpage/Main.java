package yellowpage;

import java.io.IOException;
import java.util.Map;

import yellowpage.config.ArgParser;
import yellowpage.config.YellowpageConfig;
import yellowpage.dns.DnsServer;
import yellowpage.metrics.MetricsServer;
import yellowpage.utils.Log;

import sun.misc.Signal;

public class Main {
  public static void main(String[] args) throws IOException {
    long startTime = System.currentTimeMillis();
    try {
      // Parse args and either start server or print help
      var argMap = ArgParser.parse(args);
      if(argMap.containsKey("help")){
        help();
        System.exit(0);
      }
      else{
        start(argMap, startTime);
      }
    } catch(Exception ex){
      Log.error(ex.getMessage());
      help();
      System.exit(1);
    }
  }

  private static void help(){
    System.out.println("USAGE: yellowpage [options]");
  } 

  private static void start(Map<String, String> args, long startTime) {

    try {
      Log.info("Starting Yellowpage DNS");

      Signal.handle(new Signal("TERM"), sig -> System.exit(0));
      Signal.handle(new Signal("INT"), sig -> System.exit(0));

      var config = YellowpageConfig.getInstance(args);
      startMetricsServer(config);
      startDnsServer(config);
      long end = System.currentTimeMillis();
      Log.info("Startup complete in " + (end - startTime) + "ms");
    } catch (Exception ex) {
      Log.error("Yellopage failed to start. See error below.", ex);
      System.exit(1);
    }

  }

  private static void startDnsServer(YellowpageConfig config) {
    var server = new DnsServer(config);
    server.start();
    Runtime.getRuntime().addShutdownHook(new Thread(server::close));
  }

  private static void startMetricsServer(YellowpageConfig config) {
    if (!config.isMetricsEnabled()) {
      Log.info("Metrics server disabled.");
      return;
    }

    var server = new MetricsServer(config);
    server.start();
    Runtime.getRuntime().addShutdownHook(new Thread(server::close));
  }

}