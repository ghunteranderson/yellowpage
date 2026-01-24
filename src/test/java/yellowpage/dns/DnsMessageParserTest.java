package yellowpage.dns;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import yellowpage.model.DnsMessageParser;


public class DnsMessageParserTest {

  @ParameterizedTest(name = "test_echo[{index}]")
  @CsvSource({
    "be d9 01 20 00 01 00 00 00 00 00 01 03 77 77 77 06 67 6f 6f 67 6c 65 03 63 6f 6d 00 00 01 00 01 00 00 29 04 d0 00 00 00 00 00 0c 00 0a 00 08 e0 aa 43 b1 07 f6 80 7e",
    // TODO: The following test can parse but we do not compress for the response. Need to revisit this.
    //"00 02 81 80 00 01 00 06 00 00 00 01 03 77 77 77 06 67 6f 6f 67 6c 65 03 63 6f 6d 00 00 01 00 01 c0 0c 00 01 00 01 00 00 01 2a 00 04 8e fb ba 67 c0 0c 00 01 00 01 00 00 01 2a 00 04 8e fb ba 63 c0 0c 00 01 00 01 00 00 01 2a 00 04 8e fb ba 93 c0 0c 00 01 00 01 00 00 01 2a 00 04 8e fb ba 6a c0 0c 00 01 00 01 00 00 01 2a 00 04 8e fb ba 69 c0 0c 00 01 00 01 00 00 01 2a 00 04 8e fb ba 68 00 00 29 04 d0 00 00 00 00 00 00"
  })
  public void test_echo(String inputHex){
    // Arrange
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