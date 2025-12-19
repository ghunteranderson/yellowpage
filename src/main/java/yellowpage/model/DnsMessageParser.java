package yellowpage.model;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yellowpage.model.DnsMessage.DnsQuestion;
import yellowpage.model.DnsMessage.DnsResourceRecord;
import yellowpage.utils.ByteReader;
import yellowpage.utils.ByteWriter;

public class DnsMessageParser {

  public static DnsMessage fromBytes(byte[] data) {
    var reader = new ByteReader(data);

    // Parse Header
    int h_id = reader.nextShort();
    int h_flags = reader.nextShort();
    int h_qdcount = reader.nextShort();
    int h_ancount = reader.nextShort();
    int h_nscount = reader.nextShort();
    int h_arcount = reader.nextShort();

    var questions = readQuestions(reader, h_qdcount);
    var answerRecords = readDnsResourceRecords(reader, h_ancount);
    var authorityRecords = readDnsResourceRecords(reader, h_nscount);
    var additionalRecords = readDnsResourceRecords(reader, h_arcount);

    if(reader.remaining() > 0)
      throw new IllegalStateException("DNS message parser error. Expected end of message but saw more bytes.");

    // Build output
    return DnsMessage.builder()
      .txId(h_id)
      .flags(new DnsFlags(h_flags))
      .questions(Arrays.asList(questions))
      .answerRecords(Arrays.asList(answerRecords))
      .authoritiesRecords(Arrays.asList(authorityRecords))
      .additionalRecords(Arrays.asList(additionalRecords))
      .build();

  }

  public static byte[] toBytes(DnsMessage answer) {
    var out = new ByteWriter();

    // Write Header
    out.writeShort(answer.getTxId());
    out.writeShort(answer.getFlags().getRawFlags());
    out.writeShort(answer.getQuestions().size());
    out.writeShort(answer.getAnswerRecords().size());
    out.writeShort(answer.getAuthoritiesRecords().size());
    out.writeShort(answer.getAdditionalRecords().size());

    writeQuestions(out, answer.getQuestions());
    writeDnsResourceRecords(out, answer.getAnswerRecords());
    writeDnsResourceRecords(out, answer.getAuthoritiesRecords());
    writeDnsResourceRecords(out, answer.getAdditionalRecords());

    return out.toByteArray();
  }

  private static DnsQuestion[] readQuestions(ByteReader reader, int qcount) {
    var questions = new DnsQuestion[qcount];
    for (int i = 0; i < qcount; i++) {
      questions[i] = readQuestion(reader);
    }
    return questions;
  }

  private static void writeQuestions(ByteWriter out, List<DnsQuestion> questions){
    for(var question : questions){
      writeQuestion(out, question);
    }
  }

  private static DnsQuestion readQuestion(ByteReader reader) {
    // Read QNAME
    var names = new ArrayList<String>();
    int nameSize;
    while ((nameSize = reader.nextByte()) > 0) {
      names.add(reader.nextString(nameSize));
    }

    // Read QTYPE/QCLASS
    var qtype = reader.nextShort();
    var qclass = reader.nextShort();

    // Write outpout
    return DnsQuestion.builder()
      .names(names)
      .type(qtype)
      .clazz(qclass)
      .build();
  }

  private static void writeQuestion(ByteWriter out, DnsQuestion question){
    // Write QNAME
    for(var part : question.getNames()){
      var bytes = part.getBytes(StandardCharsets.UTF_8);
      out.writeByte(bytes.length);
      out.writeBytes(bytes);
    }
    out.writeByte(0);

    // Write QTYPE/QCLASS
    out.writeShort(question.getType());
    out.writeShort(question.getClazz());
  }

  private static DnsResourceRecord[] readDnsResourceRecords(ByteReader reader, int count) {
    var records = new DnsResourceRecord[count];
    for (int i = 0; i < count; i++) {
      records[i] = readResourceRecord(reader);
    }
    return records;
  }

  private static void writeDnsResourceRecords(ByteWriter out, List<DnsResourceRecord> records){
    for(var record : records){
      writeDnsResourceRecord(out, record);
    }
  }

  private static DnsResourceRecord readResourceRecord(ByteReader reader) {
    // Read QNAME
    var names = new ArrayList<String>();
    int nameSize;
    while ((nameSize = reader.nextByte()) > 0) {
      names.add(reader.nextString(nameSize));
    }

    // Read QTYPE/QCLASS
    var r_type = reader.nextShort();
    var r_class = reader.nextShort();
    var r_ttl = reader.nextInt();
    var r_dataLength = reader.nextShort();
    var r_data = reader.nextBytes(r_dataLength);

    // Write outpout
    return DnsResourceRecord.builder()
      .names(names)
      .type(r_type)
      .clazz(r_class)
      .ttl(r_ttl)
      .data(r_data)
      .build();
  }

  private static void writeDnsResourceRecord(ByteWriter out, DnsResourceRecord record){
    // Write QNAME
    for(var part : record.getNames()){
      var bytes = part.getBytes(StandardCharsets.UTF_8);
      out.writeByte(bytes.length);
      out.writeBytes(bytes);
    }
    out.writeByte(0);

    // Write QTYPE/QCLASS/etc
    out.writeShort(record.getType());
    out.writeShort(record.getClazz());
    out.writeInt(record.getTtl());

    // Write data
    var data = record.getData();
    if(data == null){
      out.writeShort(0);
    }
    else{
      out.writeShort(data.length);
      out.writeBytes(data);
    }
  }



}
