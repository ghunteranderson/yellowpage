package yellowpage.exceptions;


public class YellowPageException extends RuntimeException {

  public YellowPageException(String message){
    super(message);
  }

  public YellowPageException(String message, Exception ex){
    super(message, ex);
  }
  
}
