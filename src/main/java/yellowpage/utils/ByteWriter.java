package yellowpage.utils;

import java.io.ByteArrayOutputStream;

public class ByteWriter {

  private final ByteArrayOutputStream data;

  public ByteWriter() {
    this.data = new ByteArrayOutputStream();
  }

  public void writeByte(int value) {
    data.write(value);
  }

  public void writeShort(int value){
    data.write((value >> 8) & 0xFF);
    data.write(value & 0xFF);
  }

  public void writeInt(long value){
    data.write((int)((value >> 24) & 0xFF));
    data.write((int)((value >> 16) & 0xFF));
    data.write((int)((value >> 8) & 0xFF));
    data.write((int)(value & 0xFF));
  }

  public void writeBytes(byte[] bytes) {
    this.data.writeBytes(bytes);
  }

  public byte[] toByteArray(){
    return data.toByteArray();
  }

}