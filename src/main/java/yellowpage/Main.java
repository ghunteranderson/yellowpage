package yellowpage;

import java.io.IOException;

import yellowpage.config.ArgParser;
import yellowpage.config.YellowpageConfig;
import yellowpage.dns.DnsServer;
import yellowpage.metrics.MetricsServer;
import yellowpage.utils.Log;

import sun.misc.Signal;

public class Main {
  public static void main(String[] args) throws IOException {
    long startTime = System.currentTimeMillis();
    // Parse args and either start server or print help
    var parser = getArgParser();
    var parsed = parser.parse(args);

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
      parser.help(System.out);
      System.exit(0);
      return;
    }

    try {
      var config = YellowpageConfig.getInstance(parsed.args);
      start(config, startTime);
    } catch (Exception ex) {
      Log.error(ex.getMessage());
      help();
      System.exit(1);
    }
  }

  private static ArgParser getArgParser() {
    return new ArgParser()
        .withArg("ip")
        .defaults("0.0.0.0")
        .description("Host IP to bind UDP listener to.")
        .register()

        .withArg("port")
        .defaults("53")
        .description("Host port for accepting DNS queries.")
        .register()

        .withArg("zones")
        .defaults("/etc/yellowpage/zones.d/")
        .description(
            "List of directories or files where Yellowpage zone files can be found. Separate multiple paths with a commma.")
        .register()

        .withArg("forward.address")
        .defaults("1.1.1.1")
        .description("Upstream DNS server for queries not answered by the local zone files.")
        .register()

        .withArg("forward.port")
        .defaults("53")
        .description("Port of upstream DNS. See --forward.address")
        .register()

        .withArg("metrics.enabled")
        .defaults("true")
        .description("Enables Prometheus style metrics server at http://localhost:{metrics.port}/metrics.")
        .register()

        .withArg("metrics.port")
        .defaults("9053")
        .description("Port of metrics HTTP server. See --metrics.enabled")
        .register()
        
        .withArg("help")
        .description("Display this help screen.")
        .register();

  }

  private static void help() {
    System.out.println("USAGE: yellowpage [options]");
  }

  private static void start(YellowpageConfig config, long startTime) {

    try {
      Log.info("Starting Yellowpage DNS");

      Signal.handle(new Signal("TERM"), sig -> System.exit(0));
      Signal.handle(new Signal("INT"), sig -> System.exit(0));

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