package yellowpage.dns;

import lombok.RequiredArgsConstructor;
import yellowpage.dispatch.DnsRequestHandler;
import yellowpage.exceptions.YellowPageException;
import yellowpage.model.DnsMessage;
import yellowpage.model.DnsQuestion;
import yellowpage.model.DnsRecordType;
import yellowpage.repos.DnsRepo;

@RequiredArgsConstructor
public class DnsHandlerImpl implements DnsRequestHandler{

  private final DnsRepo repo;

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
    var record = repo.query(host, DnsRecordType.A).findFirst();

    if(record.isEmpty()){
      return StandardResponses.noDataNotAuthority(request);
    }
    else {
      return StandardResponses.addressV4Answer(request, record.get());
    }
  }
  
}
