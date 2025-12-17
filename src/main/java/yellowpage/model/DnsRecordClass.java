package yellowpage.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DnsRecordClass {

  IN(1),
  CS(2),
  CH(3),
  HS(4),
  START(255),
  UNKNOWN(-1);

  public static DnsRecordClass forCode(int code){
    for(var e : DnsRecordClass.values()){
      if(e.code == code)
        return e;
    }
    return UNKNOWN;
  }

  private final int code;
  
}
