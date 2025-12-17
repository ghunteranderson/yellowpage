package yellowpage.repos;

import lombok.Data;
import yellowpage.model.DnsRecordType;

@Data
public class DnsRecord {
  
  private final String host;
  private final DnsRecordType type;
  private final String value;
  private final int ttl;

}
