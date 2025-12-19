package yellowpage.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DnsMessage {
  private final int txId;
  private final DnsFlags flags;
  @Builder.Default
  private final List<DnsQuestion> questions = List.of();
  @Builder.Default
  private final List<DnsResourceRecord> answerRecords = List.of();
  @Builder.Default
  private final List<DnsResourceRecord> authoritiesRecords = List.of();
  @Builder.Default
  private final List<DnsResourceRecord> additionalRecords = List.of();

  @Data
  @Builder
  public static class DnsQuestion {
    private final List<String> names;
    private final int type;
    private final int clazz;
  }

  @Data
  @Builder
  public static class DnsResourceRecord {
    private final List<String> names;
    private final int type;
    private final int clazz;
    private final long ttl;
    private final byte[] data;
  }

}
