package yellowpage.dns;

import java.util.List;

import yellowpage.model.DnsClass;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsMessage.DnsResourceRecord;
import yellowpage.model.DnsRCode;
import yellowpage.model.DnsRecordType;
import yellowpage.model.Zone;

public class DnsResponseBuilder {

  public static DnsMessage nonExistentDomain(DnsMessage request) {
    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(true)
        .recusionAvailable(true)
        .responseCode(DnsRCode.NAME_ERROR)
        .build();

    return DnsMessage.builder()
        .txId(request.getTxId())
        .flags(flags)
        .questions(request.getQuestions())
        .build();

  }

  public static DnsMessage refused(DnsMessage request) {
    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(true)
        .recusionAvailable(true)
        .responseCode(DnsRCode.REFUSED)
        .build();

    return DnsMessage.builder()
        .txId(request.getTxId())
        .flags(flags)
        .questions(request.getQuestions())
        .build();
  }

  public static DnsMessage serverError(DnsMessage request) {
    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(true)
        .recusionAvailable(true)
        .responseCode(DnsRCode.SERVER_ERROR)
        .build();

    return DnsMessage.builder()
        .txId(request.getTxId())
        .flags(flags)
        .questions(request.getQuestions())
        .build();
  }

  public static DnsMessage noDataNotAuthority(DnsMessage request) {
    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(false)
        .recusionAvailable(true)
        .responseCode(DnsRCode.NO_ERROR)
        .build();

    return DnsMessage.builder()
        .txId(request.getTxId())
        .flags(flags)
        .questions(request.getQuestions())
        .build();
  }

  public static DnsMessage addressV4Answer(DnsMessage request, Zone.Record record) {

    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(true)
        .recusionAvailable(true)
        .responseCode(DnsRCode.NO_ERROR)
        .build();

    // Convert IPv4 string to 4 bytes
    var octets = record.getValue().split("\\.");
    byte[] ipData = new byte[4];
    for (int i = 0; i < octets.length; i++) {
      ipData[i] = (byte) Integer.parseInt(octets[i]);
    }

    var answer = DnsResourceRecord.builder()
        .type(DnsRecordType.A.getCode())
        .clazz(DnsClass.IN.getCode())
        .ttl(record.getTtl())
        .names(List.of(record.getName().split("\\.")))
        .data(ipData)
        .build();

    return DnsMessage.builder()
        .txId(request.getTxId())
        .flags(flags)
        .questions(request.getQuestions())
        .answerRecords(List.of(answer))
        .build();
  }

  public static DnsMessage forward(
      DnsMessage request,
      DnsResourceRecord forwarderAuthority,
      List<DnsResourceRecord> forwarderAddresses) {

    var flags = request.getFlags()
        .copy()
        .query(false)
        .authoritative(false)
        .recusionAvailable(false)
        .responseCode(DnsRCode.NO_ERROR)
        .build();

    return DnsMessage.builder()
        .txId(request.getTxId())
        .flags(flags)
        .questions(request.getQuestions())
        .authoritiesRecords(List.of(forwarderAuthority))
        .additionalRecords(forwarderAddresses)
        .build();
  }

}
