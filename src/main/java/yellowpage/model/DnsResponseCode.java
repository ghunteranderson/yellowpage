package yellowpage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DnsResponseCode {

  NO_ERROR(0),
  FORMAT_ERROR(1),
  SERVER_ERROR(2),
  NAME_ERROR(3),
  NOT_IMPLEMENTED(4),
  REFUSED(5),
  UNKNOWN(-1);

  private final int code;

  public static DnsResponseCode forCode(int code){
    for(var e : DnsResponseCode.values()){
      if(e.code == code)
        return e;
    }
    return UNKNOWN;
  }
  
}
