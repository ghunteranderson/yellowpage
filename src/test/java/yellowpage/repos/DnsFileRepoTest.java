package yellowpage.repos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import yellowpage.model.DnsRecordType;

public class DnsFileRepoTest {
  
  @Test
  void test_getAll_parses(){
    // Arrange
    var repo = new DnsFileRepo(List.of("src/test/resources/test-records/beta.txt"));
    // Act
    var recordOpt = repo.getAll().findFirst();
    // Assert
    assertTrue(recordOpt.isPresent());
    var record = recordOpt.get();
    assertEquals("node-1.beta.internal", record.getHost());
    assertEquals("10.0.0.21", record.getValue());
    assertEquals(DnsRecordType.A, record.getType());
    assertEquals(3600, record.getTtl());
  }

  @Test
  void test_query_host(){
    // Arrange
    var repo = new DnsFileRepo(List.of("src/test/resources/test-records/alpha.txt"));
    // Act
    var recordOpt = repo.query("node-2.alpha.internal").findFirst();
    // Assert
    assertTrue(recordOpt.isPresent());
    var record = recordOpt.get();
    assertEquals("node-2.alpha.internal", record.getHost());
    assertEquals("10.0.0.12", record.getValue());
    assertEquals(DnsRecordType.A, record.getType());
    assertEquals(3600, record.getTtl());
  }

    @Test
  void test_query_hostAndType(){
    // Arrange
    var repo = new DnsFileRepo(List.of("src/test/resources/test-records/alpha.txt"));
    // Act
    var recordOpt = repo.query("node-2.alpha.internal", DnsRecordType.CNAME).findFirst();
    // Assert
    assertTrue(recordOpt.isEmpty());
  }
  
}
