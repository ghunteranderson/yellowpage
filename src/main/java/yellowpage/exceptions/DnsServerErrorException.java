package yellowpage.exceptions;

public class DnsServerErrorException extends YellowpageException {

  public DnsServerErrorException(String message) {
    super(message);
  }

  public DnsServerErrorException(String message, Exception ex){
    super(message, ex);
  }
  
}
