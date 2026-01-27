package yellowpage.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import lombok.extern.java.Log;
import yellowpage.exceptions.YellowpageException;
import yellowpage.metrics.Metrics;

@Log
public class BlockingUdpConnector implements UdpConnector {

  private final DatagramChannel channel;
  private final CounterDataPoint udpErrorCounter;

  public BlockingUdpConnector(InetSocketAddress address) {
    try {
      this.channel = DatagramChannel.open();
      this.channel.bind(address);
      channel.configureBlocking(false);
    } catch(IOException ex){
      throw new YellowpageException("Could not create UdpConnector.", ex);
    }
    this.udpErrorCounter = Metrics.getUdpError();
  }

  @Override
  public void send(UdpMessage message) {
    var buffer = ByteBuffer.wrap(message.data);
    try {
      channel.send(buffer, message.address);
    } catch(IOException ex){
      this.udpErrorCounter.inc();
      throw new IllegalStateException("Could not send UDP message.", ex);
    }
  }

  @Override
  public UdpMessage receive() {
    var buffer = ByteBuffer.allocate(65535); // TODO Consider making this a thread-local resource
    SocketAddress addr = null;
    try {
      addr = channel.receive(buffer);
    } catch(IOException ex){
      this.udpErrorCounter.inc();
      throw new YellowpageException("Could not receive UDP message.", ex);
    }

    if(addr != null){
      buffer.flip();
      byte[] data = new byte[buffer.remaining()];
      buffer.get(data);
      buffer.clear();
      var message = new UdpMessage(data, addr);
      return message;
    }
    else {
      return null; // TODO: Is this really a "BlockingUdpConnector" and does it need to be? Review name/behavior.
    }
  }

}
