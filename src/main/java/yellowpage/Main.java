package yellowpage;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import yellowpage.config.YellowPageConfig;
import yellowpage.dispatch.UdpDispatcher;
import yellowpage.dispatch.UdpListener;
import yellowpage.dns.DnsHandlerImpl;
import yellowpage.repos.DnsRepoFactory;

public class Main {
    public static void main(String[] args) throws IOException {

        var config = YellowPageConfig.getInstance();
        var port = config.server().port();
        var ip = config.server().ip();
        
        System.out.printf("Starting Yellowpage DNS server...\n", port);
        System.out.printf("port=%s\n", port);

        // Setup Handler
        var repo = DnsRepoFactory.newInstance();
        var handler = new DnsHandlerImpl(repo);
        var dispatcher = new UdpDispatcher(handler);

        var channel = DatagramChannel.open();
        channel.bind(new InetSocketAddress(ip, port));
        channel.configureBlocking(false);
        new UdpListener(channel, dispatcher).start();
        channel.close();
        
        System.out.println("Shutting down.");
    }
}