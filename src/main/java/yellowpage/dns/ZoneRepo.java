package yellowpage.dns;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import yellowpage.config.YellowpageConfig;
import yellowpage.model.Zone;
import yellowpage.model.ZoneParser;
import yellowpage.utils.Log;
import yellowpage.utils.TaskRunner;

public class ZoneRepo {

  private final File[] paths;
  private Map<String, Zone> cache;
  private Map<String, Instant> previousLastModifiedTimes;

  public ZoneRepo(YellowpageConfig config, TaskRunner taskRunner){
    this(List.of(config.getZoneDirectory()), taskRunner);
  }

  public ZoneRepo(List<String> paths, TaskRunner taskRunner){
    this.paths = paths.stream().map(File::new).toArray(i -> new File[i]);
    cache = Map.of();
    previousLastModifiedTimes = new HashMap<>();
    
    taskRunner.once(this::refreshAll, 0, TimeUnit.SECONDS).join();
    taskRunner.repeat(this::refreshAll, 30, TimeUnit.SECONDS);
  }

  public List<Zone> getZonesByDomain(String domain) {
    return cache.values().stream()
      .filter(z -> domain.equals(z.getZone()) || domain.endsWith("." + z.getZone()))
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
          Log.warn("Ignoring DNS file location. Does not exist: " +  p.getAbsolutePath());
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
      Log.warn("No DNS records found.");
  }

  private Zone readFile(File file){
    
    var absPath = file.getAbsolutePath();
    var lastModified = Instant.ofEpochMilli(file.lastModified());
    var previousLastMofied = previousLastModifiedTimes.computeIfAbsent(file.getAbsolutePath(), k -> Instant.MIN);

    // If file hasn't changed, stop here
    if(lastModified.compareTo(previousLastMofied) <=0)
      return cache.get(absPath);

    
    // Reading lines
    Log.info("Reloading zone file: " + file.getPath());
    
    // Read DNS records
    Zone out;
    try {
      out = ZoneParser.fromFile(file);
    } catch(Exception ex){
      Log.error("Failed to read zone file: " + file.getAbsolutePath(), ex);
      return null;
    }
      
    // Update time stamp
    previousLastModifiedTimes.put(file.getAbsolutePath(), lastModified);
    return out;

  }


  
}
