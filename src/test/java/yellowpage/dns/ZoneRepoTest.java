package yellowpage.dns;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import yellowpage.utils.TaskRunner;

public class ZoneRepoTest {

  @Test
  void test_noMatches() throws IOException {
    try(var taskRunner = new TaskRunner()){
      var repo = new ZoneRepo(List.of("src/test/resources/zones"), taskRunner);
      var zones = repo.getZonesByDomain("www.example.com");
      assertTrue(zones.isEmpty());
    }
  }

  @Test
  void test_matchBeta() throws IOException {
    try(var taskRunner = new TaskRunner()){
      var repo = new ZoneRepo(List.of("src/test/resources/zones"), taskRunner);
      var zones = repo.getZonesByDomain("dns.beta.internal");
  
      assertEquals(1, zones.size());
      assertEquals("beta.internal", zones.get(0).getZone());
    }
  }
  
}
