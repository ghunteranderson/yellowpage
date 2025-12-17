package yellowpage.dns;

import java.util.List;

import yellowpage.model.DnsMessage;
import yellowpage.model.DnsRecordClass;
import yellowpage.model.DnsRecordType;
import yellowpage.model.DnsResourceRecord;
import yellowpage.model.DnsResponseCode;
import yellowpage.repos.DnsRecord;

public class StandardResponses {

  
  // public DnsMessage noData(DnsMessage request){

  // }

  // public DnsMessage nonExistentDomain(DnsMessage request){

  // }

  public static DnsMessage noDataNotAuthority(DnsMessage request){
    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(false)
        .recusionAvailable(false)
        .responseCode(DnsResponseCode.NO_ERROR)
        .build();

    return DnsMessage.builder()
      .txId(request.getTxId())
      .flags(flags)
      .build();
  }

  public static DnsMessage addressV4Answer(DnsMessage request, DnsRecord record){
    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(true)
        .recusionAvailable(true)
        .responseCode(DnsResponseCode.NO_ERROR)
        .build();

    // Convert IPv4 string to 4 bytes
    var octets = record.getValue().split("\\.");
    byte[] ipData = new byte[4];
    for(int i=0; i<octets.length; i++){
      ipData[i] = (byte) Integer.parseInt(octets[i]);
    }

    var answer = DnsResourceRecord.builder()
          .type(DnsRecordType.A.getCode())
          .clazz(DnsRecordClass.IN.getCode())
          .ttl(record.getTtl())
          .names(List.of(record.getHost().split("\\.")))
          .data(ipData)
          .build();

    return DnsMessage.builder()
      .txId(request.getTxId())
      .flags(flags)
      .questions(request.getQuestions())
      .answerRecords(List.of(answer))
      .build();
  }
  
}
