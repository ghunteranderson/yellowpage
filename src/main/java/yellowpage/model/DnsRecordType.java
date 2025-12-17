package yellowpage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DnsRecordType {
  A(1),      // a host address
  NS(2),     // an authoritative name server
  MD(3),     // a mail destination (Obsolete - use MX)
  MF(4),     // a mail forwarder (Obsolete - use MX)
  CNAME(5),  // the canonical name for an alias
  SOA(6),    // marks the start of a zone of authority
  MB(7),     // a mailbox domain name (EXPERIMENTAL)
  MG(8),     // a mail group member (EXPERIMENTAL)
  MR(9),     // a mail rename domain name (EXPERIMENTAL)
  NULL(10),  // a null RR (EXPERIMENTAL)
  WKS(11),   // a well known service description
  PTR(12),   // a domain name pointer
  HINFO(13), // host information
  MINFO(14), // mailbox or mail list information
  MX(15),    // mail exchange
  TXT(16),   // text strings
  AAAA(28),  // 

  // QTYPES
  AXFR(252),
  MAILB(253),
  MAILA(254),
  STAR(255),

  UNKNOWN(-1);


  public static DnsRecordType forCode(int code){
    for(var e : DnsRecordType.values()){
      if(e.code == code)
        return e;
    }
    return UNKNOWN;
  }

  private final int code;

}