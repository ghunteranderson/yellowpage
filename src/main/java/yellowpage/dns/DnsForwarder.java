package yellowpage.dns;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.RequiredArgsConstructor;

public class DnsForwarder implements DnsMessageHandler {

  private static final AtomicInteger NEXT_TX_ID = new AtomicInteger(1);
  private static final int PENDING_BUFFER_MAX_SIZE = 200;

  private final LinkedList<PendingRequest> pendingRequests = new LinkedList<>();
  private final SocketAddress forwardAddr;

  public DnsForwarder(SocketAddress dest) {
    this.forwardAddr = dest;
  }

  @Override
  public void handleInboundDnsMessage(MessageContext ctx) {

    // Find pending request
    var dnsMessage = ctx.getMessage();
    var txId = dnsMessage.getTxId();
    PendingRequest pendingReq = null;
    for (var req : pendingRequests) {
      if (req.forwardedTxId == txId) {
        pendingReq = req;
        break;
      }
    }

    if (pendingReq != null) {

      pendingRequests.remove(pendingReq);
      // TODO: I probalby shouldn't be forwarding this message as-is
      // Review authoritative/recursive flags
      // Foward to original client
      var clientTxId = pendingReq.originalTxId;
      var clientAddr = pendingReq.originalAddress;
      var clientResp = dnsMessage.withTxId(clientTxId);

      ctx.send(clientResp, clientAddr);
    }
  }

  public void forwardQuery(MessageContext ctx) {

    var clientMesg = ctx.getMessage();
    var clientAddr = ctx.getSourceAddr();
    var clientTxId = clientMesg.getTxId();
    
    // Issue new unique TX ID.
    var forwardTxId = NEXT_TX_ID.getAndIncrement();
    var forwardMesg = clientMesg.withTxId(forwardTxId);

    // Store original request for aync UDP response
    var pendingRequest = new PendingRequest(clientAddr, clientTxId, forwardTxId);
    cachePendingRequest(pendingRequest);

    // Request DNS answer from upstream
    ctx.send(forwardMesg, forwardAddr);
  }

  private void cachePendingRequest(PendingRequest req) {

    pendingRequests.add(req);

    // Prevent buffer from getting too big by evicting oldest records.
    //
    // TODO: A TTL + max size here may be helpful. A More responsive eviction
    // would reduce idle memory footprint and may propogate to client
    // before they timeout.
    if (pendingRequests.size() > PENDING_BUFFER_MAX_SIZE) {
      synchronized (pendingRequests) {
        var size = pendingRequests.size();
        var overage = size - PENDING_BUFFER_MAX_SIZE;
        for (int i = 0; i < overage; i++)
          pendingRequests.remove(0);
      }
    }
  }

  @RequiredArgsConstructor
  private static class PendingRequest {
    private final SocketAddress originalAddress;
    private final int originalTxId;
    private final int forwardedTxId;
  }

}
