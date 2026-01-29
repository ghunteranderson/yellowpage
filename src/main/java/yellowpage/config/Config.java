package yellowpage.config;

import java.util.Collections;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Config {

  private final Map<String, String> extraValues;

  public Config(){
    this(Collections.emptyMap());
  }

  public ConfigValue get(String key){
    return new ConfigValue(key, getRaw(key));
  }
  
  private String getRaw(String key){
    String value = extraValues.get(key);

    if(value != null)
      return value;

    var prefixedKey = "yp." + key;

    if((value = System.getProperty(prefixedKey)) != null)
      return value;

    if((value = System.getenv(prefixedKey)) != null)
      return value;

    String envKey = prefixedKey.toUpperCase().replace('.', '_').replace('-', '_');
    return System.getenv(envKey);
  }
  
}
