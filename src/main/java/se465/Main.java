package se465;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
  static String CONFIDENCE = "--confidence";
  static String SUPPORT = "--support";
  static String CALLGRAPH = "--callgraph";
  static String OUTPUT = "--output";
  static HashMap<String, String> options;
  static String callGraphStartWith = "Call graph node for function:";
  static String funcCallStartWith = "CS<0x";
  static String splitter = " ";
  static HashMap<String, Integer> singleSupport = new HashMap<>();
  static HashMap<String, Integer> pairSupport = new HashMap<>();
  static HashMap<String, ArrayList<String>> callGraph = new HashMap<>();
  static HashMap<String, ArrayList<String>> confidencePairs= new HashMap<>();
  public static void main(String[] args) {
      options = commandParser(args);

//      for (Map.Entry<String, String> entry: options.entrySet()){
//        System.out.println("key: " + entry.getKey() + " Val: " + entry.getValue());
//      }

    getSupports(options.get(CALLGRAPH));
    getConfidencePairs();
    inferBugs();
//    for (Map.Entry<String, Integer> entry: singleSupport.entrySet()){
//      System.out.println("Single: " + entry.getKey() + " Val: " + entry.getValue());
//    }
//
//    for (Map.Entry<String, Integer> entry: pairSupport.entrySet()){
//      System.out.println("Pair: " + entry.getKey() + " Val: " + entry.getValue());
//    }
//
//    for (Map.Entry<String, ArrayList<String>> entry: callGraph.entrySet()){
//      System.out.println("Caller: " + entry.getKey() + " callee: ");
//      for (String callee: entry.getValue()){
//        System.out.println("   " + callee);
//      }
//    }
//    for (Map.Entry<String, ArrayList<String>> entry: confidencePairs.entrySet()){
//      for (String callee: entry.getValue()){
//        System.out.println("Pair: " + entry.getKey() + ", " + callee);
//      }
//    }
  }

  static void inferBugs(){
    try {
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(options.get(OUTPUT)), StandardCharsets.US_ASCII);
      BufferedWriter bw = new BufferedWriter(outputStreamWriter);
      String format = "bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%%n";
      for (Map.Entry<String, ArrayList<String>> entry : callGraph.entrySet()) {
        for (String callee : entry.getValue()) {
          ArrayList<String> confidencePair = confidencePairs.getOrDefault(callee, null);
          if (confidencePair != null) {
            for (String pair : confidencePair) {
              if (!entry.getValue().contains(pair)) {
                //Should appear as a pair but did not
                String[] pairArray = {callee, pair};
                Arrays.sort(pairArray);
                String confidenceKey = String.join(" ", pairArray[0], pairArray[1]);
//                System.out.printf((format) + "%n", callee, entry.getKey(), pairArray[0], pairArray[1], pairSupport.get(confidenceKey), calculateConfidence(callee, confidenceKey));
                bw.write(String.format(format, callee, entry.getKey(), pairArray[0], pairArray[1], pairSupport.get(confidenceKey), calculateConfidence(callee, confidenceKey)));
              }
            }
          }
        }
      }
      bw.close();
    }catch (IOException e){
      e.printStackTrace();
    }
  }


  static void getConfidencePairs(){
    for (Map.Entry<String, Integer> pairEntry: pairSupport.entrySet()){
      String[] funcPair = pairEntry.getKey().split(splitter);
      int T_CONFIDENCE = Integer.parseInt(options.get(CONFIDENCE));
      int T_SUPPORT = Integer.parseInt(options.get(SUPPORT));

      for (int i=0; i<2; i++){
//        System.out.println("func pair: " + funcPair[0] + ", " + funcPair[1] + " with cal confidence: " + calculateConfidence(funcPair[i], pairEntry.getKey()) );
        if (pairSupport.get(pairEntry.getKey()) >= T_SUPPORT && calculateConfidence(funcPair[i], pairEntry.getKey()) >= T_CONFIDENCE){
          //Only print bugs with confidence T_CONFIDENCE or more and with pair support T_SUPPORT times or more
          //We confirm that func must always appear as a pair

          if (!confidencePairs.containsKey(funcPair[i])){
            confidencePairs.put(funcPair[i], new ArrayList<>());
          }

          confidencePairs.get(funcPair[i]).add(funcPair[1-i]);
        }
      }
    }

  }

  static float calculateConfidence(String func, String pair){
    int singleVal = singleSupport.get(func);
    int pairVal = pairSupport.get(pair);
//    float val = pairVal/singleVal;
    return pairVal * 100/ (float) singleVal;
  }

  static void getSupportsForOneFuncCall(HashSet<String> functionsCalled){
    if (functionsCalled == null) return;

    //Add support for one single function
    for (String func: functionsCalled){
      if (!singleSupport.containsKey(func)){
        singleSupport.put(func, 1);
      } else {
        int val = singleSupport.get(func);
        singleSupport.put(func, val+1);
      }
    }
    //Add support for every pair

    ArrayList<String> funcList = new ArrayList<>(functionsCalled);
    Collections.sort(funcList);
    for (int i = 0; i < funcList.size() - 1; i++){
      for (int j = i+1; j < funcList.size(); j++){
        //Now sorted, the pair should be good
        String pair = funcList.get(i) + splitter + funcList.get(j);
        if (!pairSupport.containsKey(pair)){
          pairSupport.put(pair, 1);
        } else {
          int val = pairSupport.get(pair);
          pairSupport.put(pair, val+1);
        }
      }
    }
  }

  static void getSupports(String fileName) {
    try{
      InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8);
      BufferedReader br = new BufferedReader(isr);
      HashSet<String> functionCall = null;
      String caller = null;
      for (String line; (line = br.readLine()) != null;){
        //We are trying to get the function name
        String[] lineSplit = line.split("'");
        //The second element should always be the function name
        String funcName = null;
        if (lineSplit.length >= 2){
          funcName = lineSplit[1];
        }


        if (line.startsWith(callGraphStartWith)) {
          //Process the previous function call
          getSupportsForOneFuncCall(functionCall);
          //Assign caller and create the callGraph
          if (caller != null){
            ArrayList<String> callees = new ArrayList<>(functionCall);
            callGraph.put(caller, callees);
          }
          caller = funcName;

          //Indicates a call graph for one function.
          functionCall = new HashSet<>();

          continue;
        }

        if (functionCall != null && funcName != null){
          //Now we should be at the first line of the function call graph
          //The second element should always be the function name
          functionCall.add(funcName);
        }
      }

      //Last function call not captured by the for loop
      getSupportsForOneFuncCall(functionCall);
      //Assign caller and create the callGraph
      if (caller != null){
        ArrayList<String> callees = new ArrayList<>(functionCall);
        callGraph.put(caller, callees);
      }
      br.close();

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  static HashMap<String, String> commandParser(String[] args) {
    if(args.length > 8) throw new IllegalArgumentException("erroneous flag");

    HashMap<String, String> arguments = new HashMap<>();
    // Put default values
    arguments.put(SUPPORT, null);
    arguments.put(CONFIDENCE, null);
    arguments.put(CALLGRAPH, null);
    arguments.put(OUTPUT, null);

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
