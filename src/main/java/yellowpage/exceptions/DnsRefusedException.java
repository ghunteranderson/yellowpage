package yellowpage.exceptions;

public class DnsRefusedException extends YellowpageException {

  public DnsRefusedException(String message) {
    super(message);
  }

  public DnsRefusedException(String message, Exception ex){
    super(message, ex);
  }
  
}
