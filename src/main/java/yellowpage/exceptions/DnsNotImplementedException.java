package yellowpage.exceptions;

public class DnsNotImplementedException extends YellowpageException {

  public DnsNotImplementedException(String message) {
    super(message);
  }

  public DnsNotImplementedException(String message, Exception ex){
    super(message, ex);
  }
  
}
