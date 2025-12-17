package yellowpage.repos;

import lombok.extern.slf4j.Slf4j;
import yellowpage.config.YellowPageConfig;

@Slf4j
public class DnsRepoFactory {

  public static DnsRepo newInstance(){
    var config = YellowPageConfig.getInstance();
    
    var paths = config.fileDiscovery().paths();
    log.info("Monitoring file paths for DNS records: {}", paths);

    return new DnsFileRepo(paths);
  }
  
}
