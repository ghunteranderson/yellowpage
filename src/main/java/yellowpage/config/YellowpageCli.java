package yellowpage.config;

import java.io.OutputStream;

public class YellowpageCli {

  private static final ArgParser PARSER = new ArgParser()
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

  public static ArgParser.ParsedArgs parseArgs(String[] args) {
    return PARSER.parse(args);
  }

  public static void printHelp(OutputStream out){
    PARSER.help(out);
  }

}
