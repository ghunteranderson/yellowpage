package yellowpage.config;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import yellowpage.exceptions.YellowpageException;

public class ArgParser {

  private final List<ArgSpec> specs = new ArrayList<>();

  public ArgBuilder withArg(String key){
    return this.new ArgBuilder(key);
  }

  public ParsedArgs parse(String [] args){
    var argMap = new HashMap<String, String>();
    var errors = parseRawArgs(args, argMap);
    applyDefaults(argMap);
    return new ParsedArgs(argMap, errors);
  }

  /**
   * 
   * @param args List of arguments to be parsed
   * @param out  Destination of parsed arguments
   * @return     List of errors encountered
   */
  private List<String> parseRawArgs(String[] args, Map<String, String> out){
    var errors = new ArrayList<String>();

    var scanner = new ArrayDeque<>(Arrays.asList(args));

    while(!scanner.isEmpty()){
      var rawKey = scanner.pollFirst();

      // ### There are no positional arguments.
      if(!rawKey.startsWith("--")){
        errors.add("Expected option but found positional argument: " + rawKey);
        continue;
      }

      // ### Parse the key and value parts out
      var key = rawKey.substring(2); // Remove leading -- from key
      var value = "";

      // --option=value
      if(rawKey.contains("=")){
        var splitIdx = key.indexOf('=');
        
        // Key must have at least one character --=vlaue
        if(splitIdx == 0)
          errors.add("Invalid option: " + rawKey);

        value = key.substring(splitIdx+1);
        key = key.substring(0, splitIdx);
      }
      // --option value
      else if(!scanner.isEmpty() && !scanner.peekFirst().startsWith("--")){
        value = scanner.pollFirst();
      }
      // --option
      else {
        value = "true";
      }

      // ### Check if argument is known before adding to map
      var valid = false;
      for(var spec : specs){
        if(spec.key.equals(key)){
          valid = true;
          break;
        }
      }
      if(!valid){
        errors.add("Invalid argument: " + rawKey);
      }
      else {
        out.put(key, value);
      }
    }

    return errors;
  }

  private void applyDefaults(Map<String, String> args){
    for(var spec : specs){
      if(!args.containsKey(spec.key) && spec.defaultValue != null)
        args.put(spec.key, spec.defaultValue);
    }
  }

  public void help(OutputStream out){

    try(var writer = new PrintWriter(out)){

      writer.append("USAGE: yellowpage [options]");
      writer.append("A simple DNS server for non critical networks.");
      writer.append('\n');

      var indent = specs
        .stream()
        .map(s -> s.key.length())
        .reduce(0, Integer::max) + 6; // 2 for -- and 4 for padding between columns
  
      var format1 = "%1$-" + indent + "s%2$s%n";
      for(var spec : specs){
        writer.printf(format1, "--" + spec.key, spec.description);
        var defaultText = spec.defaultValue == null ? "Default: none" : "Default: " + spec.defaultValue;
        writer.print(defaultText.indent(indent)); 
        writer.println();
      }
    }
    
  }

  private class ArgSpec {
    private String key;
    private String description;
    private String defaultValue;
  }

  public class ArgBuilder {
    private final ArgSpec spec;
    private boolean registered;

    private ArgBuilder(String key){
      this.spec = new ArgSpec();
      this.spec.key = key;
    }

    public ArgBuilder description(String description){
      spec.description = description;
      return this;
    }
    
    public ArgBuilder defaults(String value){
      spec.defaultValue = value;
      return this;
    }

    public ArgParser register(){
      if(!registered){
        ArgParser.this.specs.add(spec);
        registered = true;
        return ArgParser.this;
      }
      else {
        throw new YellowpageException("Argument already registered. Cannot register again.");
      }
    }
  }

  @RequiredArgsConstructor
  public static class ParsedArgs {
    public final Map<String, String> args;
    public final List<String> errors;
  }
  
}
