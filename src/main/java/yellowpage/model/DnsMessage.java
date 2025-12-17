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

  public String toLogString() {
    var s = new StringBuilder();

    s.append("{id=").append(txId).append(',')
      .append("flags=").append(flags).append(',')
      .append("qd=[");

    if (questions != null) {
      for (int i = 0; i < questions.size(); i++) {
      if (i > 0) s.append(',');
      DnsQuestion q = questions.get(i);
      s.append(q == null ? "null" : q.toLogString());
      }
    }

    s.append("],an=[");

    if (answerRecords != null) {
      for (int i = 0; i < answerRecords.size(); i++) {
      if (i > 0) s.append(',');
      DnsResourceRecord r = answerRecords.get(i);
      s.append(r == null ? "null" : r.toLogString());
      }
    }

    s.append("],ns=[");

    if (authoritiesRecords != null) {
      for (int i = 0; i < authoritiesRecords.size(); i++) {
      if (i > 0) s.append(',');
      DnsResourceRecord r = authoritiesRecords.get(i);
      s.append(r == null ? "null" : r.toLogString());
      }
    }

    s.append("],ar=[");

    if (additionalRecords != null) {
      for (int i = 0; i < additionalRecords.size(); i++) {
      if (i > 0) s.append(',');
      DnsResourceRecord r = additionalRecords.get(i);
      s.append(r == null ? "null" : r.toLogString());
      }
    }

    s.append("]}");
    return s.toString();

  }
}
