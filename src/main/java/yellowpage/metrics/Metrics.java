package yellowpage.metrics;

import io.prometheus.metrics.core.datapoints.CounterDataPoint;
import io.prometheus.metrics.core.datapoints.GaugeDataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.model.snapshots.Unit;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public class Metrics {

  private static final Counter DNS_ANSWERED = Counter.builder()
    .name("yp_dns_answered")
    .help("The number of answered DNS queries including NXDOMAIN.")
    .labelNames("kind")
    .register();

  private static final Gauge DNS_FORWARD_PENDING = Gauge.builder()
    .name("yp_dns_forward_pending")
    .help("Number of forwarded DNS queries awaiting a response.")
    .register();

  private static final Counter DNS_FORWARD_DROPPED = Counter.builder()
    .name("yp_dns_forward_dropped")
    .help("Number of forwarded DNS queries that have been dropped due to no response.")
    .register();

  private static final Counter DNS_ERROR = Counter.builder()
    .name("yp_dns_error")
    .help("Number of DNS error responses. This excludes malformed DNS requests that fail to parse.")
    .register();

  private static final Counter DNS_RESPONSE_SECONDS = Counter.builder()
    .name("yp_dns_response_seconds")
    .unit(Unit.SECONDS)
    .help("Total time to respond to DNS query.")
    .register();

  private static final Counter DNS_RESPONSE_SECONDS_COUNT = Counter.builder()
    .name("yp_dns_response_seconds_count")
    .help("Number of responses in yp_dns_response_seconds")
    .register();

  private static final Counter UDP_ERROR = Counter.builder()
    .name("yp_udp_error")
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

  public static CounterDataPoint getDnsRespSeconds(){
    return DNS_RESPONSE_SECONDS.labelValues();
  }

  public static CounterDataPoint getDnsRespSecondsCount(){
    return DNS_RESPONSE_SECONDS_COUNT.labelValues();
  }

  public static CounterDataPoint getUdpError(){
    return UDP_ERROR.labelValues();
  }
  
}
