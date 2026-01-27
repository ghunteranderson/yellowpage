package yellowpage.metrics;

import java.io.IOException;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import lombok.extern.java.Log;
import yellowpage.config.YellowpageConfig;
import yellowpage.exceptions.YellowpageException;

@Log
public class MetricsServer {

  private HTTPServer prometheus;

  public MetricsServer(YellowpageConfig config){
    if(!config.isMetricsEnabled()){
      log.info("Metrics disabled. Configure with yp.metrics.enabled");  
      return;
    }

    try {
      var port = config.getMetricsPort();
      this.prometheus = HTTPServer.builder()
            .port(port)
            .buildAndStart();
  
      log.info("Metrics available: http://localhost:" + prometheus.getPort() + "/metrics");
    } catch(IOException ex) {
      throw new YellowpageException("Could not start metrics server.", ex);
    }
  }

  public void stop(){
    if(this.prometheus != null)
      this.prometheus.stop();
  }
  
}
