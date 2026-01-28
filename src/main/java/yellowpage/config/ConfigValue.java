package yellowpage.config;

import java.net.InetAddress;
import java.util.Optional;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigValue {

  @Getter
  private final String key;
  private final String value;

  public Optional<String> asString(){
    if(value == null || value.isBlank())
      return Optional.empty();
    else
      return Optional.of(value);
  }

  public Optional<Integer> asInt(){
    return asString()
      .map(valueSafe -> {
        try {
          return Integer.parseInt(valueSafe);
        } catch(NumberFormatException ex){
          throw new IllegalArgumentException("Config " + key + " is not a valid integer: " + valueSafe);
        }
      });
  }

  public Optional<InetAddress> asIPv4(){
    return asString()
      .map(valueSafe -> {
        try {

          var parts = value.split("\\.");
          byte[] address = new byte[4];

          address[0] = Byte.parseByte(parts[0]);
          address[1] = Byte.parseByte(parts[1]);
          address[2] = Byte.parseByte(parts[2]);
          address[3] = Byte.parseByte(parts[3]);

          return InetAddress.getByAddress(address);
          
        } catch(Exception ex){
          throw new IllegalArgumentException("Config " + key + " is not a value IPv4 value: " + value, ex);
        }
      });
  }

  public Optional<Boolean> asBoolean(){
    return asString().map(Boolean::parseBoolean);
  }

  
  
}
