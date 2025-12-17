package yellowpage.dispatch;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UdpListener {

  private final DatagramChannel channel;
  private final Consumer<UdpInbound> handler;

  public void start() throws IOException {
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
      this.handler.accept(new UdpInbound(data, addr, this::send));
    }
  }

  private void send(UdpOutbound message) {
    var buffer = ByteBuffer.wrap(message.data);
    try {
      channel.send(buffer, message.destination);
    } catch(IOException ex){
      throw new IllegalStateException("Could not send UDP message.", ex);
    }
  }

  @RequiredArgsConstructor
  public static class UdpInbound {
    public final byte[] data;
    public final SocketAddress source;
    private final Consumer<UdpOutbound> responseHandler;

    public void send(byte[] bytes){
      responseHandler.accept(new UdpOutbound(bytes, source));
    }

  }

  @RequiredArgsConstructor
  public static class UdpOutbound {
    public final byte[] data;
    public final SocketAddress destination;
  }
}
