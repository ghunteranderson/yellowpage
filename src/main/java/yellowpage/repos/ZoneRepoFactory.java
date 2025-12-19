package yellowpage.repos;

import java.util.List;

import lombok.extern.java.Log;
import yellowpage.config.YellowPageConfig;

@Log
public class ZoneRepoFactory {

  public static ZoneRepo newInstance(){
    var config = YellowPageConfig.getInstance();
    
    var paths = List.of(config.getZoneDirectory());
    log.info(() -> "Monitoring file paths for DNS records: " + paths);

    return new ZoneFileRepo(paths);
  }
  
}
