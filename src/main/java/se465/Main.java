package se465;

import java.io.*;
import java.util.*;

public class Main {
  static String CONFIDENCE = "--confidence";
  static String SUPPORT = "--support";
  static String CALLGRAPH = "--callgraph";
  static String OUTPUT = "--output";
  static HashMap<String, String> options;
  static String funcCallStartWith = "CS<0x";
  static String splitter = " ";

  public static void main(String[] args){
    options = commandParser(args);
    BugDetector detector = new BugDetector(options);
    detector.getSupports(options.get(Main.CALLGRAPH));
    detector.getConfidencePairs();
    detector.inferBugs();
  }

  static HashMap<String, String> commandParser(String[] args) {
    HashMap<String, String> arguments = new HashMap<>();
    // Put default values
    arguments.put(SUPPORT, null);
    arguments.put(CONFIDENCE, null);
    arguments.put(CALLGRAPH, null);
    arguments.put(OUTPUT, null);

    for (int i = 1; i < args.length; i++) {
      //In our program, we always assume it is an argument-value pair.
      String arg = args[i-1];
      //Update index here to make sure we always have i points to an argument at the beginning of the loop
      String value = args[i];
      i++;

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
