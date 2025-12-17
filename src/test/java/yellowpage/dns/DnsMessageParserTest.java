package yellowpage.dns;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.junit.jupiter.api.Test;

import yellowpage.model.DnsMessageParser;


public class DnsMessageParserTest {

  // @ParameterizedTest(name = "test_echo[{index}]")
  // @CsvSource({
  //   "be d9 01 20 00 01 00 00 00 00 00 01 03 77 77 77 06 67 6f 6f 67 6c 65 03 63 6f 6d 00 00 01 00 01 00 00 29 04 d0 00 00 00 00 00 0c 00 0a 00 08 e0 aa 43 b1 07 f6 80 7e"
  // })
  @Test
  public void test_echo(){
    // Arrange
    String inputHex = "be d9 01 20 00 01 00 00 00 00 00 01 03 77 77 77 06 67 6f 6f 67 6c 65 03 63 6f 6d 00 00 01 00 01 00 00 29 04 d0 00 00 00 00 00 0c 00 0a 00 08 e0 aa 43 b1 07 f6 80 7e";
    var hexFormat = HexFormat.ofDelimiter(" ");
    var inputBytes = hexFormat.parseHex(inputHex);

    // Act
    var message = DnsMessageParser.fromBytes(inputBytes);
    var outputBytes = DnsMessageParser.toBytes(message);

    // Assert
    var outputHex = hexFormat.formatHex(outputBytes);
    assertEquals(inputHex, outputHex);
  }

}