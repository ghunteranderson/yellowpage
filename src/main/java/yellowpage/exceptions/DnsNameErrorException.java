package yellowpage.exceptions;

public class DnsNameErrorException extends YellowpageException {

  public DnsNameErrorException(String message) {
    super(message);
  }

  public DnsNameErrorException(String message, Exception ex){
    super(message, ex);
  }
  
}
