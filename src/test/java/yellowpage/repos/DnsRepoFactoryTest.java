package yellowpage.repos;

import org.junit.jupiter.api.Test;

public class DnsRepoFactoryTest {

  @Test
  void test(){
    var repo = DnsRepoFactory.newInstance();
    repo.getAll().forEach(System.out::println);
  }
  
}
