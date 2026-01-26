package yellowpage.dns;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import yellowpage.config.YellowpageConfig;
import yellowpage.exceptions.YellowpageException;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsRecordType;
import yellowpage.model.Zone;
import yellowpage.model.DnsMessage.DnsQuestion;

@Log
@RequiredArgsConstructor
public class DnsResolver implements DnsMessageHandler {

  private final ZoneRepo repo;
  private final DnsForwarder forwarder;

  public DnsResolver(YellowpageConfig config, DnsForwarder forwarder){
    this(new ZoneRepo(config), forwarder);
  }

  @Override
  public void handleInboundDnsMessage(MessageContext ctx) {
      answerOrForward(ctx);
  }

  private void answerOrForward(MessageContext ctx) {

    var clientMesg = ctx.getMessage();

    // Extract DNS question
    var questions = clientMesg.getQuestions();
    if (questions.size() == 0)
      throw new YellowpageException("No questions in query.");
    if (questions.size() > 1)
      throw new YellowpageException("Multiple questions in query: " + questions.size());

    // Lookup matching zone
    var question = questions.get(0);
    var host = String.join(".", question.getNames());
    var zones = repo.getZonesByDomain(host);

    // Use DNS forwarder
    if (zones.isEmpty()) {
      forwarder.forwardQuery(ctx);
    }

    // We can answer authoritatively
    else {
      var record = queryZones(question, host, zones);
      ctx.reply(buildAnswer(record, clientMesg));
    }

  }

  private DnsMessage buildAnswer(Zone.Record record, DnsMessage clientMesg) {
    // Build DNS answer
    if (record == null)
      return DnsResponseBuilder.nonExistentDomain(clientMesg);
    else
      return DnsResponseBuilder.addressV4Answer(clientMesg, record);
  }

  private Zone.Record queryZones(DnsQuestion question, String host, List<Zone> zones) {
    Zone.Record record = null;
    for (var zone : zones) {
      record = zone.getRecords(host)
          .filter(r -> r.getType() == DnsRecordType.A)
          .findFirst()
          .orElse(null);
      if (record != null) {
        break;
      }
    }

    return record;
  }

}
