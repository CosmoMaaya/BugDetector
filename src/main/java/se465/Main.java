package se465;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class Main {
  static HashMap<String, String> options;
  public static void main(String[] args) {
      options = parse(args);

      for (Map.Entry<String, String> entry: options.entrySet()){
        System.out.println("key: " + entry.getKey() + " Val: " + entry.getValue());
      }
  }

  public int add(int a, int b) {
    return a + b;
  }

  static HashMap<String, String> parse(String[] args) {
    if(args.length > 8) throw new IllegalArgumentException("erroneous flag");

    HashMap<String, String> arguments = new HashMap<>();
    // Put default values
    arguments.put("--support", null);
    arguments.put("--confidence", null);
    arguments.put("--callgraph", null);
    arguments.put("--output", null);

    for (int i = 0; i < args.length; i++) {
      //In our program, we always assume it is an argument-value pair.
      String arg = args[i];
      //Update index here to make sure we always have i points to an argument at the beginning of the loop
      String value = args[++i];

      //First, check if it is a valid argument
      if (!arguments.containsKey(arg)) {
        throw new IllegalArgumentException("erroneous flags");
      }

      //Second, check if it is duplicated
      if (arguments.get(arg) != null) {
        throw new IllegalArgumentException("duplicate flags");
      }

      //Third, we need to check the value. The type of values depends on which argument it is
      switch (arg) {
        case "--support":
          //Positive 32-bit signed integer
          int t_support;
          try {
            t_support = Integer.parseInt(value);
          } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid value");
          }
          if (t_support <= 0) throw new IllegalArgumentException("invalid value");
          break;
        case "--confidence":
          //An integer between 0 and 100 (inclusive) to specify t_confidence;
          int t_confidence;
          try {
            t_confidence = Integer.parseInt(value);
          } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid value");
          }
          if (t_confidence < 0 || t_confidence > 100) throw new IllegalArgumentException("invalid value");
          break;
        case "--callgraph":
          //the file of the call graph, produced by LLVM opt
          File callgraph = new File(value);
          if (!callgraph.exists() || callgraph.isDirectory() || !callgraph.canRead()) {
            throw new IllegalArgumentException("invalid value");
          }
          break;
        case "--output":
          //the file to save the result
          File output = new File(value);
          if (output.isDirectory() || output.exists() && !output.canWrite()) {
            throw new IllegalArgumentException("invalid value");
          }
          break;
      }

      //put the value in map
      arguments.put(arg, value);
    }

    //Finally check if we missed any flags
    for (String val: arguments.values()){
      if (val == null) throw new IllegalArgumentException("missing flags");
    }

    return arguments;
  }
}
