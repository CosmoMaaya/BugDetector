package se465;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BugDetector {
    //Now we try to manually give an ID to each string to increase performance
    HashMap<String, Integer> singleSupport = new HashMap<>();
    HashMap<String, Integer> pairSupport = new HashMap<>();
    HashMap<String, HashSet<String>> callGraph = new HashMap<>();
    HashMap<String, HashSet<String>> confidencePairs = new HashMap<>();

    //  static ArrayList<String> funcs = new ArrayList<>();
//    HashMap<String, Integer> funcToID = new HashMap<>();
//    HashMap<Integer, String> iDToFunc = new HashMap<>();

    HashMap<String, String> options;
    String callGraphStartWith = "Call graph node for function:";

    BugDetector(HashMap<String, String> options) {
        this.options = options;
    }

    void inferBugs(){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(options.get(Main.OUTPUT)), StandardCharsets.UTF_8);
            BufferedWriter bw = new BufferedWriter(outputStreamWriter);
            String format = "bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%%n";
            for (Map.Entry<String, HashSet<String>> entry : callGraph.entrySet()) {
                for (String callee : entry.getValue()) {
                    HashSet<String> confidencePair = confidencePairs.getOrDefault(callee, null);
                    if (confidencePair != null) {
                        for (String pair : confidencePair) {
                            if (!entry.getValue().contains(pair)) {
                                //Should appear as a pair but did not
                                String[] pairArray = {callee, pair};
                                Arrays.sort(pairArray);
                                String confidenceKey = pairArray[0] + Main.splitter + pairArray[1];
//                                System.out.printf((format) + "%n", callee, entry.getKey(), pairArray[0], pairArray[1], pairSupport.get(confidenceKey), calculateConfidence(callee, confidenceKey));
                                bw.write(String.format(format, callee, entry.getKey(), pairArray[0], pairArray[1], pairSupport.get(confidenceKey), calculateConfidence(callee, confidenceKey)));
                            }
                        }
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid value");
        }
    }


    void getConfidencePairs() {
        for (Map.Entry<String, Integer> pairEntry : pairSupport.entrySet()) {
            String[] funcPair = pairEntry.getKey().split(Main.splitter);
//            Integer[] funcPair = {pairEntry.getKey().left, pairEntry.getKey().right};
            int T_CONFIDENCE = Integer.parseInt(options.get(Main.CONFIDENCE));
            int T_SUPPORT = Integer.parseInt(options.get(Main.SUPPORT));

            for (int i = 0; i < 2; i++) {
//        System.out.println("func pair: " + funcPair[0] + ", " + funcPair[1] + " with cal confidence: " + calculateConfidence(funcPair[i], pairEntry.getKey()) );
                if (pairSupport.get(pairEntry.getKey()) >= T_SUPPORT && calculateConfidence(funcPair[i], pairEntry.getKey()) >= T_CONFIDENCE) {
                    //Only print bugs with confidence T_CONFIDENCE or more and with pair support T_SUPPORT times or more
                    //We confirm that func must always appear as a pair

                    if (!confidencePairs.containsKey(funcPair[i])) {
                        confidencePairs.put(funcPair[i], new HashSet<>());
                    }

                    confidencePairs.get(funcPair[i]).add(funcPair[1 - i]);
                }
            }
        }

    }

    float calculateConfidence(String func, String pair) {
        int singleVal = singleSupport.getOrDefault(func, -1);
        int pairVal = pairSupport.getOrDefault(pair, -1);

        if (singleVal == -1 || pairVal == -1) return -1;
//    float val = pairVal/singleVal;
        return pairVal * 100 / (float) singleVal;
    }

    void getSupportsForOneFuncCall(HashSet<String> functionsCalled) {
        if (functionsCalled == null) return;

        //Add support for one single function
        for (String func : functionsCalled) {
            if (!singleSupport.containsKey(func)) {
                singleSupport.put(func, 1);
            } else {
                int val = singleSupport.get(func);
                singleSupport.put(func, val + 1);
            }
        }
        //Add support for every pair

        ArrayList<String> funcList = new ArrayList<>(functionsCalled);
        Collections.sort(funcList);
        for (int i = 0; i < funcList.size() - 1; i++) {
            String first = funcList.get(i);
            for (int j = i + 1; j < funcList.size(); j++) {
                //Our pair will automatically sort
//                Pair pair = new Pair(funcList.get(i), funcList.get(j));
                String pair = first + Main.splitter + funcList.get(j);
                if (!pairSupport.containsKey(pair)) {
                    pairSupport.put(pair, 1);
                } else {
                    int val = pairSupport.get(pair);
                    pairSupport.put(pair, val + 1);
                }
            }
        }

//        for (Map.Entry<String, Integer> entry: pairSupport.entrySet()){
//            System.out.println("'" + entry.getKey() + "' with value: " + entry.getValue());
//        }
    }

    void getSupports(String fileName) {
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            HashSet<String> functionCall = null;
            String caller = null;
//            int callerID = 0;
//            int id = 0;
//            int funcID = id;
            for (String line; (line = br.readLine()) != null; ) {
                //We are trying to get the function name
                String[] lineSplit = line.split("'");
                //The second element should always be the function name
                String funcName = null;

                if (lineSplit.length >= 2) {
                    funcName = lineSplit[1];

//                    if (!funcToID.containsKey(funcName)) {
//                        funcToID.put(funcName, id);
////            assert !iDToFunc.containsKey(id);
//                        iDToFunc.put(id, funcName);
//                        funcID = id;
//                        id++;
//                    } else {
////            assert iDToFunc.get(id)
//                        funcID = funcToID.get(funcName);
//                    }
                }

                if (line.startsWith(callGraphStartWith)) {
                    //Process the previous function call
                    getSupportsForOneFuncCall(functionCall);
                    //Assign caller and create the callGraph
                    if (caller != null) {
//            ArrayList<String> callees = new ArrayList<>(functionCall);
                        callGraph.put(caller, functionCall);
                    }
                    caller = funcName;
//                    callerID = funcID;

                    //Indicates a call graph for one function.
                    functionCall = new HashSet<>();

                    continue;
                }

                if (functionCall != null && funcName != null) {
                    //Now we should be at the first line of the function call graph
                    //The second element should always be the function name
                    functionCall.add(funcName);
                }
            }

            //Last function call not captured by the for loop
            getSupportsForOneFuncCall(functionCall);
            //Assign caller and create the callGraph
            if (caller != null) {
//        ArrayList<String> callees = new ArrayList<>(functionCall);
                callGraph.put(caller, functionCall);
            }
            br.close();
        } catch (IOException e) {
            throw new IllegalArgumentException("invalid value");
        }
    }

}