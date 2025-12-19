package yellowpage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DnsClass {

  IN(1),
  CS(2),
  CH(3),
  HS(4),
  START(255),
  UNKNOWN(-1);

  public static DnsClass forCode(int code){
    for(var e : DnsClass.values()){
      if(e.code == code)
        return e;
    }
    return UNKNOWN;
  }

  private final int code;
  
}
