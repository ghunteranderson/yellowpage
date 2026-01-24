package yellowpage.dns;


public interface DnsMessageHandler  {
  
  /**
   * @param udpMessage
   * @param dnsMessage
   */
  void handleInboundDnsMessage(MessageContext ctx);
}
