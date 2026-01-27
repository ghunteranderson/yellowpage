package yellowpage.metrics;

import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.datapoints.GaugeDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class Metrics {

  private static final Counter DNS_ANSWERED = Counter.builder()
    .name("yellowpage_dns_answered")
    .help("The number of answered DNS queries including NXDOMAIN.")
    .labelNames("kind")
    .register();

  private static final Gauge DNS_FORWARD_PENDING = Gauge.builder()
    .name("yellowpage_dns_forward_pending")
    .help("Number of forwarded DNS queries awaiting a response.")
    .register();

  private static final Counter DNS_FORWARD_DROPPED = Counter.builder()
    .name("yellowpage_dns_forward_dropped")
    .help("Number of forwarded DNS queries that have been dropped due to no response.")
    .register();

  private static final Counter DNS_ERROR = Counter.builder()
    .name("yellowpage_dns_error")
    .help("Number of DNS error responses. This excludes malformed DNS requests that fail to parse.")
    .register();

  private static final Gauge UDP_BUFFERED_INBOUND = Gauge.builder()
    .name("yellowpage_udp_buffered")
    .help("The number of buffered UDP messages awaiting handling.")
    .labelNames("kind")
    .register();

  private static final Counter UDP_ERROR = Counter.builder()
    .name("yellowpage_udp_error")
    .help("Number of UDP IO errors.")
    .register();

  public static CounterDataPoint getDnsAnsweredLocal(){
    return DNS_ANSWERED.labelValues("local");
  }

  public static CounterDataPoint getDnsAnsweredFoward(){
    return DNS_ANSWERED.labelValues("forward");
  }

  public static GaugeDataPoint getDnsForwardPending(){
    return DNS_FORWARD_PENDING.labelValues();
  }

  public static CounterDataPoint getDnsForwardDropped(){
    return DNS_FORWARD_DROPPED.labelValues();
  }

  public static CounterDataPoint getDnsError(){
    return DNS_ERROR.labelValues();
  }

  public static GaugeDataPoint getUdpBufferedInbound(){
    return UDP_BUFFERED_INBOUND.labelValues("inbound");
  }

  public static GaugeDataPoint getUdpBufferedOutbound(){
    return UDP_BUFFERED_INBOUND.labelValues("outbound");
  }

  public static CounterDataPoint getUdpError(){
    return UDP_ERROR.labelValues();
  }

  


  
  
  
}
