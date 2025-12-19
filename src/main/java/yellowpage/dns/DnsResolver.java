package yellowpage.dns;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import yellowpage.dispatch.DnsRequestHandler;
import yellowpage.exceptions.YellowPageException;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsRecordType;
import yellowpage.model.DnsMessage.DnsQuestion;
import yellowpage.model.Zone;
import yellowpage.repos.ZoneRepo;

@RequiredArgsConstructor
public class DnsResolver implements DnsRequestHandler{

  private final ZoneRepo repo;

  @Override
  public DnsMessage handleDnsRequest(DnsMessage request) {
    
    var questions = request.getQuestions();
    if(questions.size() == 0)
      throw new YellowPageException("No questions in query.");
    if(questions.size() > 1)
      throw new YellowPageException("Multiple questions in query: " + questions.size());

    return answerQuestion(request, questions.get(0));
  }

  private DnsMessage answerQuestion(DnsMessage request, DnsQuestion question){

    var host = String.join(".", question.getNames());
    var zones = repo.getZonesByDomain(host);
    
    Optional<Zone.Record> record = Optional.empty();
    for(var zone : zones){
      record = zone.getRecords(host)
        .filter(r -> r.getType() == DnsRecordType.A)
        .findFirst();
      if(record.isPresent()){
        break;
      }
    }

    if(record.isEmpty()){
      if(zones.isEmpty()){
        // Domain does not exist. This server is unaware of its zone
        return StandardResponses.noDataNotAuthority(request);
      }
      else {
        // Domain does not exist. We are sure since we manage it's zone
        return StandardResponses.nonExistentDomain(request);
      }
    }
    else {
      // Record found!
      return StandardResponses.addressV4Answer(request, record.get());
    }
  }
  
}
