package yellowpage.dns;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.datapoints.GaugeDataPoint;
import lombok.RequiredArgsConstructor;
import yellowpage.metrics.Metrics;
import yellowpage.model.DnsMessageParser;
import yellowpage.udp.UdpMessage;

public class DnsForwarder {

  private static final AtomicInteger NEXT_TX_ID = new AtomicInteger(1);
  private static final int PENDING_BUFFER_MAX_SIZE = 200;

  private final ConcurrentSkipListSet<PendingRequest> pendingRequests = new ConcurrentSkipListSet<>();
  private final SocketAddress forwardAddr;

  private final CounterDataPoint answerForwardCounter = Metrics.getDnsAnsweredFoward();
  private final GaugeDataPoint forwardPendingGauge = Metrics.getDnsForwardPending();
  private final CounterDataPoint forwardDroppedCounter = Metrics.getDnsForwardDropped();

  public DnsForwarder(SocketAddress dest) {
    this.forwardAddr = dest;
  }

  public void handleUpstreamResponse(DnsRequestContext ctx) {

    // Find pending request
    var dnsMessage = ctx.getRequest();
    var txId = dnsMessage.getTxId();
    PendingRequest pendingReq = null;
    for (var req : pendingRequests) {
      if (req.forwardedTxId == txId) {
        pendingReq = req;
        break;
      }
    }

    // Check if we're expecting this message
    if(pendingReq == null)
      return;
    
    // Remove from pending list.
    // Only allow one response by checking if our thread successfully removed it.
    if (pendingRequests.remove(pendingReq)) {
      forwardPendingGauge.dec();
      // TODO: I probalby shouldn't be forwarding this message as-is
      // Review authoritative/recursive flags
      // Foward to original client
      var clientTxId = pendingReq.originalTxId;
      var clientResp = dnsMessage.withTxId(clientTxId);

      // Delayed repling with the original context
      // This helps with tracking and metrics
      pendingReq.originalCtx.reply(clientResp); 
      answerForwardCounter.inc();
    }
  }

  public void forwardQuery(DnsRequestContext ctx) {

    // Issue new unique TX ID.
    var forwardTxId = NEXT_TX_ID.getAndIncrement();
    var forwardMesg = ctx.getRequest().withTxId(forwardTxId);

    // Store original request for aync UDP response
    var pendingRequest = new PendingRequest(ctx, forwardTxId);
    cachePendingRequest(pendingRequest);

    // Request DNS answer from upstream
    ctx.getUdpClient().send(UdpMessage.outbound(DnsMessageParser.toBytes(forwardMesg), forwardAddr));
    ctx.disableReplyCheck();
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
        while(pendingRequests.size() > PENDING_BUFFER_MAX_SIZE){
          var candidate = pendingRequests.getFirst();
          var removed = pendingRequests.remove(candidate);
          if(removed)
            this.forwardDroppedCounter.inc();
        }
      }
    }
    forwardPendingGauge.set(pendingRequests.size());
  }

  @RequiredArgsConstructor
  private static class PendingRequest implements Comparable<PendingRequest>{
    private final long createdAt;
    private final DnsRequestContext originalCtx;
    private final int originalTxId;
    private final int forwardedTxId;

    public PendingRequest(DnsRequestContext originalCtx, int forwardedTxId){
      this.createdAt = System.currentTimeMillis();
      this.originalCtx = originalCtx;
      this.originalTxId = originalCtx.getRequest().getTxId();
      this.forwardedTxId = forwardedTxId;
    }

    @Override
    public int compareTo(PendingRequest other) {
      return Long.compare(this.createdAt, other.createdAt);
    }
  }

}
