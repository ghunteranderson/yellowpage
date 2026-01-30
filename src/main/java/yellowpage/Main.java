package yellowpage;

import java.io.IOException;

import yellowpage.config.YellowpageCli;
import yellowpage.config.YellowpageConfig;
import yellowpage.dns.DnsServer;
import yellowpage.metrics.MetricsServer;
import yellowpage.utils.Log;

import sun.misc.Signal;

public class Main {
  public static void main(String[] args) throws IOException {
    long startTime = System.currentTimeMillis();
    // Parse args and either start server or print help
    var parsed = YellowpageCli.parseArgs(args);

    // Fail if args are not valid
    if(!parsed.errors.isEmpty()){
      for(var err : parsed.errors){
        Log.error(err);
      }
      Log.error("Run \"yellowpage --help\" for all options.");
      System.exit(1);
      return;
    }

    else if(parsed.args.containsKey("help")){
      YellowpageCli.printHelp(System.out);
      System.exit(0);
      return;
    }

    try {
      var config = YellowpageConfig.getInstance(parsed.args);
      startYellowpage(config, startTime);
    } catch (Exception ex) {
      Log.error(ex.getMessage());
      Log.error("Run \"yellowpage --help\" for all options.");
      System.exit(1);
    }
  }

  private static void startYellowpage(YellowpageConfig config, long startTime) {

    try {
      Log.info("Starting Yellowpage DNS");

      // Required for Graal native builds
      Signal.handle(new Signal("TERM"), sig -> System.exit(0));
      Signal.handle(new Signal("INT"), sig -> System.exit(0));

      startMetricsServer(config);
      startDnsServer(config);
      long end = System.currentTimeMillis();
      Log.info("Startup complete in " + (end - startTime) + "ms");
    } catch (Exception ex) {
      Log.error("Yellowpage failed to start. See error below.", ex);
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