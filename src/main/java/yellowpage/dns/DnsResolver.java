package yellowpage.dns;

import java.util.List;

import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import lombok.RequiredArgsConstructor;
import yellowpage.config.YellowpageConfig;
import yellowpage.exceptions.YellowpageException;
import yellowpage.metrics.Metrics;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsRecordType;
import yellowpage.model.Zone;
import yellowpage.model.DnsMessage.DnsQuestion;
import yellowpage.utils.TaskRunner;

@RequiredArgsConstructor
public class DnsResolver {

  private final ZoneRepo repo;
  private final DnsForwarder forwarder;
  
  private final CounterDataPoint answerLocalCounter = Metrics.getDnsAnsweredLocal();

  public DnsResolver(YellowpageConfig config, DnsForwarder forwarder, TaskRunner taskRunner){
    this(new ZoneRepo(config, taskRunner), forwarder);
  }

  public void resolve(DnsRequestContext ctx) {

    var clientMesg = ctx.getRequest();

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
      answerLocalCounter.inc();
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
          // Prioritize longest match
          .sorted((a, b) -> -1*Integer.compare(a.getName().length(), b.getName().length()))
          .findFirst()
          .orElse(null);
      if (record != null) {
        break;
      }
    }

    return record;
  }

}
