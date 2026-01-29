package yellowpage.metrics;

import java.io.IOException;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import yellowpage.config.YellowpageConfig;
import yellowpage.exceptions.YellowpageException;
import yellowpage.utils.Log;

public class MetricsServer implements AutoCloseable {

  private YellowpageConfig config;
  private HTTPServer prometheus;
  private boolean started;

  public MetricsServer(YellowpageConfig config) {
    this.config = config;
  }

  public synchronized void start() {
    if (started)
      throw new YellowpageException("Metrics server has already been started. Cannot start again.");

    try {
      var port = config.getMetricsPort();
      this.prometheus = HTTPServer.builder()
          .port(port)
          .buildAndStart();

      Log.info("Metrics exporter available: http://localhost:" + prometheus.getPort() + "/metrics");
    } catch (IOException ex) {
      throw new YellowpageException("Could not start metrics server.", ex);
    }
    this.started = true;
  }

  @Override
  public void close() {
    Log.info("Stopping metrics server.");
    if (this.prometheus != null)
      this.prometheus.stop();
  }

}
