package yellowpage.config;
import java.net.InetAddress;

import org.eclipse.microprofile.config.spi.Converter;

public class InetAddressConverter implements Converter<InetAddress> {

  @Override
  public InetAddress convert(String value) throws IllegalArgumentException, NullPointerException {
    try {

      var parts = value.split("\\.");
      byte[] address = new byte[4];

      address[0] = Byte.parseByte(parts[0]);
      address[1] = Byte.parseByte(parts[1]);
      address[2] = Byte.parseByte(parts[2]);
      address[3] = Byte.parseByte(parts[3]);

      return InetAddress.getByAddress(address);
      
    } catch(Exception ex){
      throw new IllegalArgumentException("Could not convert value: " + value, ex);
    }
  }
  
}
