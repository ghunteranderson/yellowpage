package yellowpage.dns;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import lombok.extern.java.Log;
import yellowpage.config.YellowPageConfig;
import yellowpage.model.Zone;
import yellowpage.model.ZoneParser;
import yellowpage.utils.TaskRunner;

@Log
public class ZoneRepo {

  private final File[] paths;
  private Map<String, Zone> cache;
  private Map<String, Instant> previousLastModifiedTimes;

  public ZoneRepo(YellowPageConfig config){
    this(List.of(config.getZoneDirectory()));
  }

  public ZoneRepo(List<String> paths){
    this.paths = paths.stream().map(File::new).toArray(i -> new File[i]);
    cache = Map.of();
    previousLastModifiedTimes = new HashMap<>();
    
    log.info(() -> "Monitoring file paths for DNS records: " + paths);
    TaskRunner.once(this::refreshAll, 0, TimeUnit.SECONDS).join();
    TaskRunner.repeat(this::refreshAll, 30, TimeUnit.SECONDS);
  }

  public List<Zone> getZonesByDomain(String domain) {
    return cache.values().stream()
      .filter(z -> domain.endsWith(z.getZone()))
      .sorted((z1, z2) -> Integer.compare(z2.getZone().length(), z1.getZone().length()))
      .collect(Collectors.toList());
  }

  public List<Zone> getZones() {
    return new ArrayList<>(this.cache.values());
  }

  private void refreshAll(){
    var newCache = new HashMap<String, Zone>();
    Stream.of(paths)
      // Aggregate files recursively up to depth of 1
      .flatMap(p -> {
        if(p.isFile())
          return Stream.of(p);
        else if(p.isDirectory())
          return Stream.of(p.listFiles()).filter(c -> c.isFile());
        else {
          log.warning("Ignoring DNS file location. Does not exist: " +  p.getAbsolutePath());
          return Stream.of();
        }
      })
      .forEach(file -> {
        var fullPath = file.getAbsolutePath();
        var zone = readFile(file);
        if(zone != null)
          newCache.put(fullPath, zone);
      });
    this.cache = newCache;
    
    if(this.cache.values().stream().allMatch(z -> z.getRecords().isEmpty()))
      log.warning("No DNS records found.");
  }

  private Zone readFile(File file){
    
    var absPath = file.getAbsolutePath();
    var lastModified = Instant.ofEpochMilli(file.lastModified());
    var previousLastMofied = previousLastModifiedTimes.computeIfAbsent(file.getAbsolutePath(), k -> Instant.MIN);

    // If file hasn't changed, stop here
    if(lastModified.compareTo(previousLastMofied) <=0)
      return cache.get(absPath);

    
    // Reading lines
    log.info(() -> "Detected change in zone file. Reloading: " + file.getPath());
    
    // Read DNS records
    Zone out;
    try {
      out = ZoneParser.fromFile(file);
    } catch(Exception ex){
      log.log(Level.SEVERE, ex, () -> "Failed to read zone file: " + file.getAbsolutePath());
      return null;
    }
      
    // Update time stamp
    previousLastModifiedTimes.put(file.getAbsolutePath(), lastModified);
    return out;

  }


  
}
