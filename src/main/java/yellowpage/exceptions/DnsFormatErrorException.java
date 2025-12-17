package yellowpage.exceptions;

public class DnsFormatErrorException extends YellowPageException {

  public DnsFormatErrorException(String message) {
    super(message);
  }

  public DnsFormatErrorException(String message, Exception ex){
    super(message, ex);
  }
  
}
