package yellowpage.config;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;

import lombok.Getter;
import lombok.extern.java.Log;
import yellowpage.exceptions.YellowpageException;

@Getter
@Log
public class YellowpageConfig {

  public static YellowpageConfig getInstance() {
    var config = new Config();
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
        config.get("yp.zones.path"),
        c -> c.asString().orElse("/etc/yellowpage/zones.d"));

    this.serverPort = logConfig(logBuilder,
        config.get("yp.server.port"),
        c -> c.asInt().orElse(53));

    this.serverIp = logConfig(logBuilder,
        config.get("yp.server.ip"),
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
        config.get("yp.forward.address"),
        c -> c.asIPv4()
            .orElseGet(() -> new ConfigValue("", "1.1.1.1").asIPv4().get()));

    this.forwardPort = logConfig(logBuilder,
        config.get("yp.forward.port"),
        c -> c.asInt().orElse(53));

    this.metricsEnabled = logConfig(logBuilder,
        config.get("yp.metrics.enabled"),
        c -> c.asBoolean().orElse(false));

    this.metricsPort = logConfig(logBuilder,
        config.get("yp.metrics.port"),
        c -> c.asInt().orElse(9053));

    log.info(logBuilder::toString);
  }

  private <T> T logConfig(StringBuilder log, ConfigValue config, Function<ConfigValue, T> mapper) {
    var key = config.getKey();
    var value = mapper.apply(config);
    log.append('\n').append(key).append(" = ").append(value);
    return value;
  }

}
