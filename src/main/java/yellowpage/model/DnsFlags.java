package yellowpage.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DnsFlags {

  public static DnsFlagsBuilder builder(){
    return new DnsFlagsBuilder();
  }

  public static DnsFlagsBuilder builder(int initialValue){
    return new DnsFlagsBuilder(initialValue);
  }

  private final int flags;

  public boolean isQuery(){
    return (flags & 0x8000) == 0;
  }

  public int getOpCode(){
    return (flags >> 11) & 0b1111;
  }

  public boolean isAuthoritative(){
    return (flags & 0x0400) != 0;
  }

  public boolean isTruncated(){
    return (flags & 0x0200) != 0;
  }

  public boolean isRecursionDesired(){
    return (flags & 0x0100) != 0;
  }

  public boolean isRecursionAvailable(){
    return (flags & 0x0080) != 0;
  }

  public boolean isAuthenticData(){
    return (flags & 0x0020) != 0;
  }

  public boolean isCheckingDisabled(){
    return (flags & 0x0010) != 0;
  }

  public DnsResponseCode getResponseCode(){
    int code = flags & 0b1111;
    return DnsResponseCode.forCode(code);
  }

  public int getRawFlags(){
    return this.flags;
  }

  public DnsFlagsBuilder copy(){
    return new DnsFlagsBuilder(this.flags);
  }


  public static class DnsFlagsBuilder {
    private int flags;
    
    public DnsFlagsBuilder(int flags){
      this.flags = flags;
    }

    public DnsFlagsBuilder(){
      this.flags = 0;
    }

    private DnsFlagsBuilder setBit(int mask, boolean enabled){
      if(enabled){
        flags |= mask;
      }
      else {
        flags &= ~mask;
      }
      return this;
    }

    public DnsFlagsBuilder query(boolean enabled){
      return setBit(0x8000, !enabled); // Set to 0 when it is a query
    }

    public DnsFlagsBuilder opCode(int code){
      if(code < 0 || code > 0x0F)
        throw new IllegalArgumentException("Invalid opcode: " + code);
      
      flags &= ~(0b1111 << 11);       // Turn off all 4 bits
      flags |= (code & 0b1111) << 11; // Turn on selected bits
      return this;
    }

    public DnsFlagsBuilder authoritative(boolean enabled){
      return setBit(0x0400, enabled);
    }

    public DnsFlagsBuilder truncated(boolean enabled){
      return setBit(0x0200, enabled);
    }

    public DnsFlagsBuilder recursionDesired(boolean enabled){
      return setBit(0x0100, enabled);
    }

    public DnsFlagsBuilder recusionAvailable(boolean enabled){
      return setBit(0x0080, enabled);
    }

    public DnsFlagsBuilder authenticData(boolean enabled){
      return setBit(0x0020, enabled);
    }

    public DnsFlagsBuilder checkingDisabled(boolean enabled){
      return setBit(0x0010, enabled);
    }

    public DnsFlagsBuilder responseCode(DnsResponseCode code){
      int codeInt = code.getCode();
      if(codeInt < 0 || codeInt > 0x0F)
        throw new IllegalArgumentException("Invalid response code: " + codeInt);
      flags &= ~0b1111;
      flags |= (codeInt & 0b1111);
      return this;
    }

    public DnsFlags build(){
      return new DnsFlags(flags);
    }

  }
  
}
