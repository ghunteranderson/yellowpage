package yellowpage.udp;

public interface UdpConnector {

  public void send(UdpMessage message);
  public UdpMessage receive();
  
}
