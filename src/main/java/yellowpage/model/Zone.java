package yellowpage.model;

import java.util.List;
import java.util.stream.Stream;

import lombok.Data;

@Data
public class Zone {
  private String zone;
  private int ttl;
  private List<Record> records;

  @Data
  public static class Record {
    private String name;
    private DnsRecordType type;
    private String value;
    private int ttl = -1;
  }

  public Stream<Record> getRecords(String domain){
    if(!domain.endsWith(zone))
      return Stream.empty();

    String prefix;
    int domainLength = domain.length();
    int zoneLength = zone.length();
    if(domainLength == zoneLength){
      prefix = "@";
    }
    else {
      prefix = domain.substring(0, domainLength - zoneLength - 1);
    }

    return records.stream()
      .filter(r -> r.name.equals(prefix));
  }
}