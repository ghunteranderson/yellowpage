package yellowpage.dispatch;

import yellowpage.model.DnsMessage;

public interface DnsRequestHandler {
  DnsMessage handleDnsRequest(DnsMessage request);
}
