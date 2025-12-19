package yellowpage.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.introspector.PropertyUtils;

public class ZoneParser {

  private static final Yaml yaml = buildYamlParser();

  private static Yaml buildYamlParser(){
    var loaderOptions = new LoaderOptions();
    var constructor = new Constructor(Zone.class, loaderOptions);
    constructor.setPropertyUtils(new PropertyUtils(){
      @Override
      public Property getProperty(Class<? extends Object> type, String name) {
        // Custom logic to transform the name (e.g., dash-case to camelCase)
        if (name.contains("_")) {
            name = toCamelCase(name);
        }
        return super.getProperty(type, name);
      }

      private String toCamelCase(String snakeCaseName) {
        StringBuilder builder = new StringBuilder();
        String[] parts = snakeCaseName.split("_");
        builder.append(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            builder.append(parts[i].substring(0, 1).toUpperCase(Locale.ROOT)).append(parts[i].substring(1));
        }
        return builder.toString();
      }
    });

    var yaml = new Yaml(constructor);
    return yaml;
  }

  public static Zone fromFile(File file) throws IOException {
    Zone zone = yaml.load(new FileInputStream(file));

    int defaultTtl = zone.getTtl();
    zone.getRecords().forEach(r -> {
      if(r.getTtl() < 0)
        r.setTtl(defaultTtl);
    });

    return zone;
  }
  
}
