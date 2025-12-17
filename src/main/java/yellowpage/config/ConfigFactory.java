package yellowpage.config;

import java.util.List;

import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;
import yellowpage.utils.Memo;

public class ConfigFactory {

  private static final Memo<SmallRyeConfig> INSTANCE = new Memo<>(ConfigFactory::buildNewConfig);

  public static SmallRyeConfig getInstance() {
    return INSTANCE.get();
  }

  private static SmallRyeConfig buildNewConfig(){
    var profiles = List.of(System.getProperty("smallrye.config.profile", "").trim().split(","));
    
    return new SmallRyeConfigBuilder()
        .withProfiles(profiles)
        .addDefaultSources()
        .withMapping(YellowPageConfig.class)
        .build();
  }

}
