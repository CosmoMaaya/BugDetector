package se465;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class BugDetector {
    //Now we try to manually give an ID to each string to increase performance
    HashMap<Integer, Integer> singleSupport = new HashMap<>();
    HashMap<Pair, Integer> pairSupport = new HashMap<>();
    HashMap<Integer, HashSet<Integer>> callGraph = new HashMap<>();
    HashMap<Integer, HashSet<Integer>> confidencePairs = new HashMap<>();

    //  static ArrayList<String> funcs = new ArrayList<>();
    HashMap<String, Integer> funcToID = new HashMap<>();
    HashMap<Integer, String> iDToFunc = new HashMap<>();

    HashMap<String, String> options;
    String callGraphStartWith = "Call graph node for function:";

    BugDetector(HashMap<String, String> options) {
        this.options = options;
    }

    void detectAndOutput() {
        getSupports(options.get(Main.CALLGRAPH));
        getConfidencePairs();
        inferBugs();
    }

    void inferBugs(){
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(options.get(Main.OUTPUT)), StandardCharsets.US_ASCII);
            BufferedWriter bw = new BufferedWriter(outputStreamWriter);
            String format = "bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%%n";
            for (Map.Entry<Integer, HashSet<Integer>> entry : callGraph.entrySet()) {
                for (Integer calleeID : entry.getValue()) {
                    HashSet<Integer> confidencePair = confidencePairs.getOrDefault(calleeID, null);
                    if (confidencePair != null) {
                        for (Integer pairID : confidencePair) {
                            if (!entry.getValue().contains(pairID)) {
                                //Should appear as a pair but did not
                                String[] pairArray = {iDToFunc.get(calleeID), iDToFunc.get(pairID)};
                                Arrays.sort(pairArray);
                                Pair confidenceKey = new Pair(calleeID, pairID);
//                System.out.printf((format) + "%n", callee, entry.getKey(), pairArray[0], pairArray[1], pairSupport.get(confidenceKey), calculateConfidence(callee, confidenceKey));
                                bw.write(String.format(format, iDToFunc.get(calleeID), iDToFunc.get(entry.getKey()), pairArray[0], pairArray[1], pairSupport.get(confidenceKey), calculateConfidence(calleeID, confidenceKey)));
                            }
                        }
                    }
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    void getConfidencePairs() {
        for (Map.Entry<Pair, Integer> pairEntry : pairSupport.entrySet()) {
//      String[] funcPair = pairEntry.getKey().split(splitter);
            Integer[] funcPair = {pairEntry.getKey().left, pairEntry.getKey().right};
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

    float calculateConfidence(Integer funcID, Pair pair) {
        int singleVal = singleSupport.get(funcID);
        int pairVal = pairSupport.get(pair);
//    float val = pairVal/singleVal;
        return pairVal * 100 / (float) singleVal;
    }

    void getSupportsForOneFuncCall(HashSet<Integer> functionsCalled) {
        if (functionsCalled == null) return;

        //Add support for one single function
        for (Integer func : functionsCalled) {
            if (!singleSupport.containsKey(func)) {
                singleSupport.put(func, 1);
            } else {
                int val = singleSupport.get(func);
                singleSupport.put(func, val + 1);
            }
        }
        //Add support for every pair

        ArrayList<Integer> funcList = new ArrayList<>(functionsCalled);
        for (int i = 0; i < funcList.size() - 1; i++) {
            for (int j = i + 1; j < funcList.size(); j++) {
                //Our pair will automatically sort
                Pair pair = new Pair(funcList.get(i), funcList.get(j));
//        String pair = funcList.get(i) + splitter + funcList.get(j);
                if (!pairSupport.containsKey(pair)) {
                    pairSupport.put(pair, 1);
                } else {
                    int val = pairSupport.get(pair);
                    pairSupport.put(pair, val + 1);
                }
            }
        }
    }

    void getSupports(String fileName) {
        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(fileName), StandardCharsets.US_ASCII);
            BufferedReader br = new BufferedReader(isr);
            HashSet<Integer> functionCall = null;
            String caller = null;
            int callerID = 0;
            int id = 0;
            int funcID = id;
            for (String line; (line = br.readLine()) != null; ) {
                //We are trying to get the function name
                String[] lineSplit = line.split("'");
                //The second element should always be the function name
                String funcName = null;

                if (lineSplit.length >= 2) {
                    funcName = lineSplit[1];

                    if (!funcToID.containsKey(funcName)) {
                        funcToID.put(funcName, id);
//            assert !iDToFunc.containsKey(id);
                        iDToFunc.put(id, funcName);
                        funcID = id;
                        id++;
                    } else {
//            assert iDToFunc.get(id)
                        funcID = funcToID.get(funcName);
                    }
                }

                if (line.startsWith(callGraphStartWith)) {
                    //Process the previous function call
                    getSupportsForOneFuncCall(functionCall);
                    //Assign caller and create the callGraph
                    if (caller != null) {
//            ArrayList<String> callees = new ArrayList<>(functionCall);
                        callGraph.put(callerID, functionCall);
                    }
                    caller = funcName;
                    callerID = funcID;

                    //Indicates a call graph for one function.
                    functionCall = new HashSet<>();

                    continue;
                }

                if (functionCall != null && funcName != null) {
                    //Now we should be at the first line of the function call graph
                    //The second element should always be the function name
                    functionCall.add(funcID);
                }
            }

            //Last function call not captured by the for loop
            getSupportsForOneFuncCall(functionCall);
            //Assign caller and create the callGraph
            if (caller != null) {
//        ArrayList<String> callees = new ArrayList<>(functionCall);
                callGraph.put(callerID, functionCall);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}