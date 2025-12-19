package yellowpage.config;

public class Config {

  public ConfigValue get(String key){
    return new ConfigValue(key, getRaw(key));
  }
  
  private String getRaw(String key){
    String value;

    if((value = System.getProperty(key)) != null)
      return value;

    if((value = System.getenv(key)) != null)
      return value;

    String envKey = key.toUpperCase().replace('.', '_').replace('-', '_');
    return System.getenv(envKey);
  }
  
}
