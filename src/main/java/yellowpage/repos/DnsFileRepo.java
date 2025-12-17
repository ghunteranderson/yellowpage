package yellowpage.repos;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import yellowpage.model.DnsRecordType;
import yellowpage.utils.TaskRunner;

@Slf4j
public class DnsFileRepo implements DnsRepo {

  private final File[] paths;
  private Map<String, List<DnsRecord>> cache;
  private Map<String, Instant> previousLastModifiedTimes;

  public DnsFileRepo(List<String> paths){
    this.paths = paths.stream().map(File::new).toArray(i -> new File[i]);
    cache = Map.of();
    previousLastModifiedTimes = new HashMap<>();

    TaskRunner.once(this::refreshAll, 0, TimeUnit.SECONDS).join();
    TaskRunner.repeat(this::refreshAll, 30, TimeUnit.SECONDS);
  }

  @Override
  public Stream<DnsRecord> getAll() {
    return cache
      .values()
      .stream()
      .flatMap(e -> e.stream())
      .distinct(); // TODO: This is probably O(n^2). Can we reduce this? Possibly flatten a copy during refresh?
  }

  @Override
  public Stream<DnsRecord> query(String host) {
    return getAll()
      .filter(r -> r.getHost().equals(host));
  }

  @Override
  public Stream<DnsRecord> query(String host, DnsRecordType type) {
    return getAll()
      .filter(r -> r.getHost().equals(host) && r.getType() == type);
  }

  private void refreshAll(){
    this.cache = Stream.of(paths)
      // Aggregate files recursively up to depth of 1
      .flatMap(p -> {
        if(p.isFile())
          return Stream.of(p);
        else if(p.isDirectory())
          return Stream.of(p.listFiles()).filter(c -> c.isFile());
        else {
          log.warn("Ignoring DNS file location. Does not exist: {}", p.getAbsolutePath());
          return Stream.of();
        }
      })
      .collect(Collectors.toMap(f -> f.getAbsolutePath(), f -> readFile(f)));
    
    if(this.cache.values().stream().allMatch(List::isEmpty))
      log.warn("No DNS records found.");
  }

  private List<DnsRecord> readFile(File file){
    
    var path = file.toPath();
    var absPath = file.getAbsolutePath();
    var lastModified = Instant.ofEpochMilli(file.lastModified());
    var previousLastMofied = previousLastModifiedTimes.computeIfAbsent(file.getAbsolutePath(), k -> Instant.MIN);

    // If file hasn't changed, stop here
    if(lastModified.compareTo(previousLastMofied) <=0)
      return cache.getOrDefault(absPath, List.of());

    
    // Reading lines
    log.info("Detected chance in DNS record file. Reloading: {}", file.getPath());
    List<String> lines;
    try {
      lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
    } catch(IOException ex){
      throw new RuntimeException("Could not parse lines of DNS file: " + path, ex);
    }
    
    // Read DNS records
    var mapper = new RowParser(absPath);
    var out = lines.stream()
      .map(mapper::apply)
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
      
    // Update time stamp
    previousLastModifiedTimes.put(file.getAbsolutePath(), lastModified);
    return out;
  }

  @RequiredArgsConstructor
  private static class RowParser implements Function<String, Optional<DnsRecord>> {

    private int rowCounter = 1;
    private final String path;

    @Override
    public Optional<DnsRecord> apply(String row) {
      int rowNumber = rowCounter++;

      row = row.trim();
      if(row.startsWith("#"))
        return Optional.empty();
      
      // Split tokens
      var tokens = row.split("\s+");
      if(tokens.length != 4){
        log.warn("Ignoring invalid DNS record in {}[ln{}]: wrong number of token", path, rowNumber);
        return Optional.empty();
      }

      // Token 1: Host
      String dnsRecordHost = tokens[0];
      // Token 2: Record Type
      DnsRecordType dnsRecordType = null;
      try {
        dnsRecordType = DnsRecordType.valueOf(tokens[1]);
      } catch(NoSuchElementException ex){
        log.warn("Ignoring invalid DNS record in {}[ln{}]: invalid record type: {}", path, rowNumber, tokens[1]);
        return Optional.empty();
      }
      // Token 3: Value
      String dnsRecordValue = tokens[2]; 
      // Token 4: TTL
      int dnsRecordTtl = 0;
      try {
        dnsRecordTtl = Integer.valueOf(tokens[3]);
      } catch(NumberFormatException ex){
        log.warn("Ignoring invalid DNS record in {}[ln{}]: invalid TTL value: {}", path, rowNumber, tokens[3]);
        return Optional.empty();
      }

      return Optional.of(new DnsRecord(dnsRecordHost, dnsRecordType, dnsRecordValue, dnsRecordTtl));
    }

  }

  
}
