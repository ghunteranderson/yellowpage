package yellowpage.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.function.Function;

import lombok.Getter;
import yellowpage.exceptions.YellowpageException;
import yellowpage.utils.Log;

@Getter
public class YellowpageConfig {

  public static YellowpageConfig getInstance(Map<String, String> args) {
    var config = new Config(args);
    return new YellowpageConfig(config);
  }

  private final String zoneDirectory;

  private final int serverPort;
  private final InetAddress serverIp;

  private final InetAddress forwardAddress;
  private final int forwardPort;

  private final boolean metricsEnabled;
  private final int metricsPort;

  private YellowpageConfig(Config config) {
    var logBuilder = new StringBuilder().append("Yellowpage config:");

    this.zoneDirectory = logConfig(logBuilder,
        config.get("zones.path"),
        c -> c.asString().orElse("/etc/yellowpage/zones.d"));

    this.serverPort = logConfig(logBuilder,
        config.get("server.port"),
        c -> c.asInt().orElse(53));

    this.serverIp = logConfig(logBuilder,
        config.get("server.ip"),
        c -> c.asIPv4()
      .orElseGet(() -> {
        try {
          return InetAddress.getByAddress(new byte[]{0, 0, 0, 0});
        } catch(UnknownHostException ex){
          throw new YellowpageException("Could not build address 0.0.0.0", ex);
        }
      }));

    // Default to cloudflare 1.1.1.1
    this.forwardAddress = logConfig(logBuilder,
        config.get("forward.address"),
        c -> c.asIPv4()
            .orElseGet(() -> new ConfigValue("", "1.1.1.1").asIPv4().get()));

    this.forwardPort = logConfig(logBuilder,
        config.get("forward.port"),
        c -> c.asInt().orElse(53));

    this.metricsEnabled = logConfig(logBuilder,
        config.get("metrics.enabled"),
        c -> c.asBoolean().orElse(true));

    this.metricsPort = logConfig(logBuilder,
        config.get("metrics.port"),
        c -> c.asInt().orElse(9053));

    Log.info(logBuilder.toString());
  }

  private <T> T logConfig(StringBuilder log, ConfigValue config, Function<ConfigValue, T> mapper) {
    var key = config.getKey();
    var value = mapper.apply(config);
    log.append('\n').append(key).append(" = ").append(value);
    return value;
  }

}
