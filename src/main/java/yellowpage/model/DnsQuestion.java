package yellowpage.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DnsQuestion {
  private final List<String> names;
  private final int type;
  private final int clazz;

  public String toLogString(){
    var s = new StringBuilder();
    s.append("{name=[");

    if (names != null) {
      for (int i = 0; i < names.size(); i++) {
        if (i > 0) s.append('.');
        String n = names.get(i);
        s.append(n == null ? "null" : n);
      }
    }

    s.append("],type=");
    s.append(type);
    s.append(",class=").append(clazz).append('}');
    return s.toString();
  }
}