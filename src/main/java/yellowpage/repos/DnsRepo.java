package yellowpage.repos;

import java.util.stream.Stream;

import yellowpage.model.DnsRecordType;

public interface DnsRepo {

  public Stream<DnsRecord> getAll();
  public Stream<DnsRecord> query(String host);
  public Stream<DnsRecord> query(String host, DnsRecordType type);
}
