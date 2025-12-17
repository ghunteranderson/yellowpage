package yellowpage.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DnsFlagsTest {
  

  @Test
  void test_authoritative(){
    var on = new DnsFlags(0b0000010000000000);
    var off = new DnsFlags(0b0000000000000000);

    // Value is already on/off
    assertTrue(on.isAuthoritative());
    assertFalse(off.isAuthoritative());
    // Changing value using copy builder
    assertFalse(on.copy().authoritative(false).build().isAuthoritative());
    assertTrue(off.copy().authoritative(true).build().isAuthoritative());
  }
}
