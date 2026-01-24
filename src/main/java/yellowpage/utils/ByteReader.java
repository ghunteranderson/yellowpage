package yellowpage.utils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ByteReader {

  private final byte[] data;
  private int idx;

  public ByteReader(byte[] data) {
    this.data = data;
    this.idx = 0;
  }

  public int getIdx() {
    return this.idx;
  }

  public void setIdx(int idx) {
    this.idx = idx;
  }

  public int remaining() {
    return data.length - idx;
  }

  public int nextByte() {
    assertRemaining(1);
    return data[idx++] & 0xFF;
  }

  public int nextShort() {
    assertRemaining(2);
    return (((data[idx++] & 0xFF) << 8) | (data[idx++] & 0xFF));
  }

  public long nextInt() {
    assertRemaining(4);
    return ((data[idx++] & 0xFF) << 24) |
        ((data[idx++] & 0xFF) << 16) |
        ((data[idx++] & 0xFF) << 8) |
        (data[idx++] & 0xFF);
  }

  public String nextString(int size) {
    if (size < 0) {
      throw new IllegalArgumentException("size must be non-negative");
    }
    assertRemaining(size);

    var value = new String(data, idx, size, StandardCharsets.UTF_8);
    idx += size;
    return value;
  }

  public byte[] nextBytes(int size){
    if (size < 0) {
      throw new IllegalArgumentException("size must be non-negative");
    }
    assertRemaining(size);

    var value = Arrays.copyOfRange(data, idx, idx+size);
    idx += size;
    return value;
  }
  
  public ByteReader detach(int newIndex){
    var reader = new ByteReader(data);
    reader.idx = newIndex;
    return reader;
  }

  private void assertRemaining(int requestedSize) {
    if (remaining() < requestedSize)
      throw new IndexOutOfBoundsException("Not enough bytes to read " + requestedSize + " bytes");
  }

}