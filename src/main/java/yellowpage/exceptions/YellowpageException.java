package yellowpage.exceptions;


public class YellowpageException extends RuntimeException {

  public YellowpageException(String message){
    super(message);
  }

  public YellowpageException(String message, Exception ex){
    super(message, ex);
  }
  
}
