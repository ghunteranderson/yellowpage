package yellowpage.config;

import java.net.InetAddress;
import java.util.List;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

@ConfigMapping(prefix = "yp", namingStrategy = ConfigMapping.NamingStrategy.KEBAB_CASE)
public interface YellowPageConfig {

  public static YellowPageConfig getInstance() {
    return ConfigFactory.getInstance().getConfigMapping(YellowPageConfig.class);
  }

  ServerConfig server();
  
  @WithName("discovery.file")
  FileDiscoveryConfig fileDiscovery();
  
  public interface ServerConfig {
    @WithDefault("53")
    int port();
  
    @WithDefault("127.0.0.1")
    InetAddress ip();
  }

  public interface FileDiscoveryConfig {
    
    @WithDefault("/etc/yellowpages/records.d/")
    List<String> paths();
  }

}
