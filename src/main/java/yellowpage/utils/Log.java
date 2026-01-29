package yellowpage.utils;

import java.io.PrintStream;
import java.io.PrintWriter;

public class Log {

  private static PrintStream stdout = System.out;
  private static PrintStream stderr = System.err;

  public static void setStdOut(PrintStream ps){
    Log.stdout = ps;
  }

  public static void setStdErr(PrintStream ps){
    Log.stderr = ps;
  }

  public static void info(String message){
    stdout.println("[INFO] " + message);
  }

  public static void warn(String message){
    stdout.println("[WARN] " + message);
  }

  public static void warn(String message, Throwable ex){
    var writer = new PrintWriter(stderr);
    writer.append("[WARN] ").append(message).append('\n');
    ex.printStackTrace(writer);
    writer.close();
  }

  public static void error(String message){
    stdout.println("[ERROR] " + message);
  }

  public static void error(String message, Throwable ex){
    var writer = new PrintWriter(stderr);
    writer.append("[ERROR] ").append(message).append('\n');
    ex.printStackTrace(writer);
    writer.close();
  }
}