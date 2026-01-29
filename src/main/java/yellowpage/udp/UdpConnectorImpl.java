package yellowpage.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import yellowpage.exceptions.YellowpageException;
import yellowpage.metrics.Metrics;
import yellowpage.udp.UdpMessage.UdpMessageType;

public class UdpConnectorImpl implements UdpConnector {

  private final DatagramChannel channel;
  private final CounterDataPoint udpErrorCounter;
  private final CounterDataPoint udpResponseSeconds = Metrics.getDnsRespSeconds();
  private final CounterDataPoint udpResponseCount = Metrics.getDnsRespSecondsCount();

  public UdpConnectorImpl(InetSocketAddress address) {
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
      // Metrics: Mark reply time if this was a reply
      if(message.messageType == UdpMessageType.REPLY){
        long durationMs = System.currentTimeMillis() - message.durationStartTime;
        if(durationMs >= 0){
          udpResponseSeconds.inc(durationMs / 1000.0);
          udpResponseCount.inc();
        }
      }
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
      var message = UdpMessage.inbound(data, addr);
      return message;
    }
    else {
      return null; // TODO: Is this really a "BlockingUdpConnector" and does it need to be? Review name/behavior.
    }
  }

}
