package yellowpage.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import yellowpage.exceptions.YellowpageException;

public class ArgParser {

  public static Map<String, String> parse(String[] args){

    var argMap = new HashMap<String, String>();
    for(var arg : args){

      if(!arg.startsWith("--"))
        throw new YellowpageException("Invalid argument: " + arg);

      var splitPoint = arg.indexOf('=');
      String key;
      String value;

      if(splitPoint < 0){
        key = arg.substring(2);
        value = "true";
      }
      else {
        key = arg.substring(2, splitPoint); // Remove "--"" and everything after the '=' inclusive
        value = arg.substring(splitPoint+1);
      }

      argMap.put(key, value);
    }

    return Collections.unmodifiableMap(argMap);

  }
  
}
