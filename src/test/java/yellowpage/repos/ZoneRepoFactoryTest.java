package yellowpage.repos;

import org.junit.jupiter.api.Test;

public class ZoneRepoFactoryTest {

  @Test
  void test(){
    var repo = ZoneRepoFactory.newInstance();
    repo.getZones().forEach(System.out::println);
  }
  
}
