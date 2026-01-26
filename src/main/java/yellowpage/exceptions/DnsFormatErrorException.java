package yellowpage.exceptions;

public class DnsFormatErrorException extends YellowpageException {

  public DnsFormatErrorException(String message) {
    super(message);
  }

  public DnsFormatErrorException(String message, Exception ex){
    super(message, ex);
  }
  
}
