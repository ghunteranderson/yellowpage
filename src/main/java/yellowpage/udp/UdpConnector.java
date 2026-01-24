package yellowpage.udp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Log
public class UdpConnector {

  private static final AtomicInteger NEXT_ID = new AtomicInteger(1);

  private final InetSocketAddress address;
  private final DatagramChannel channel;
  private Consumer<UdpMessage> reciever;

  public UdpConnector(InetSocketAddress address) throws IOException {
    this.address = address;
    this.channel = DatagramChannel.open();
    this.channel.bind(address);
    channel.configureBlocking(false);
  }

  public void startAsync() {
    new Thread(() -> {
      try {
        this.start();
      } catch (Exception ex){
        throw new RuntimeException(ex);
      }
    }, "UDP-Listener-" + NEXT_ID.getAndIncrement()).start();
  }

  public void start() throws IOException {
    log.info(() -> "Starting UDP listener at " + address);
    var buffer = ByteBuffer.allocate(65535);
    var currentThread = Thread.currentThread();
    while (!currentThread.isInterrupted()) {
      tryRecieve(buffer);
    }
  }

  private void tryRecieve(ByteBuffer buffer) throws IOException{
    SocketAddress addr = channel.receive(buffer);
    if(addr != null){
      buffer.flip();
      byte[] data = new byte[buffer.remaining()];
      buffer.get(data);
      buffer.clear();
      var message = new UdpMessage(data, addr);
      reciever.accept(message);
    }
  }

  public void onRecieve(Consumer<UdpMessage> reciever){
    this.reciever = reciever;
  }

  public void send(UdpMessage message) {
    var buffer = ByteBuffer.wrap(message.data);
    try {
      channel.send(buffer, message.address);
    } catch(IOException ex){
      throw new IllegalStateException("Could not send UDP message.", ex);
    }
  }

  @RequiredArgsConstructor
  public static class UdpMessage {
    public final byte[] data;
    public final SocketAddress address;
  }

}
