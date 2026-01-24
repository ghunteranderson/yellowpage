package yellowpage.config;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Optional;

public class YellowPageConfig {

  public static YellowPageConfig getInstance() {
    return new YellowPageConfig();
  }

  private final Config config = new Config();

  public String getZoneDirectory() {
    return config.get("yp.zones.path").asString().orElse("/etc/yellowpage/zones.d");
  }

  public int getServerPort() {
    return config.get("yp.server.port").asInt().orElse(53);
  }

  public Optional<InetAddress> getServerIp() {
    return config.get("yp.server.ip").asIPv4();
  }

  public InetSocketAddress getForwarderAddress() {
    var inetAddr = config
        .get("yp.forwarder.primaryAddress")
        .asIPv4()
        .orElseGet(() -> new ConfigValue("", "1.1.1.1").asIPv4().get());
        
    return new InetSocketAddress(inetAddr, 53);
  }

  public int getForwarderTtl(){
    return config.get("yp.forwarder.ttl").asInt().orElse(3600);
  }

}
