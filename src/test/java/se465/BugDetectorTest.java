package se465;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

public class BugDetectorTest {

    private HashMap<String, String> options;
    private BugDetector bugDetector;

    @Before
    public void setUp(){
        options = new HashMap<>();
        options.put(Main.SUPPORT, "3");
        options.put(Main.CONFIDENCE, "75");
        options.put(Main.OUTPUT, "src/input/sample.out");
        options.put(Main.CALLGRAPH, "src/input/testCallGraph.txt");

        bugDetector = new BugDetector(options);

        File NonReadable = new File("src/input/non-writable-output");
        NonReadable.setReadable(false);
        NonReadable.setWritable(false);
    }

    @After
    public void cleanup() {
        File NonReadable = new File("src/input/non-writable-output");
        NonReadable.setReadable(true);
        NonReadable.setWritable(true);
    }

    @Test
    public void testInferBugs(){
        HashMap<String, Integer> pair = new HashMap<String, Integer>() {
            {
                put("A B", 3);
                put("B C", 3);
            }
        };

        HashMap<String, Integer> single = new HashMap<String, Integer>(){
            {
                put("A", 10);
                put("B", 4);
                put("C", 10);
            }
        };

        HashMap<String, HashSet<String>> callGraph = new HashMap<String, HashSet<String>>(){
            {
                put("scope1", new HashSet<String>() {
                    {add("B");}
                });
            }
        };

        HashMap<String, HashSet<String>> confidencePairs = new HashMap<String, HashSet<String>>() {
            {
                put("B", new HashSet<String>() {
                    {
                        add("A"); add("C");
                    }
                });
            }
        };

        bugDetector.pairSupport = pair;
        bugDetector.singleSupport = single;
        bugDetector.confidencePairs = confidencePairs;
        bugDetector.callGraph = callGraph;

        bugDetector.inferBugs();
        HashSet<String> lines = new HashSet<>();

        try {
            InputStreamReader isr = new InputStreamReader(new FileInputStream(options.get(Main.OUTPUT)), StandardCharsets.US_ASCII);
            BufferedReader br = new BufferedReader(isr);

//            String line = br.readLine();
            for (String line; (line = br.readLine()) != null; ) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashSet<String> expectedLines = new HashSet<String>(){
            {
                add("bug: B in scope1, pair: (A, B), support: 3, confidence: 75.00%");
                add("bug: B in scope1, pair: (B, C), support: 3, confidence: 75.00%");
            }
        };

        Assert.assertEquals(expectedLines, lines);
    }

    @Test
    public void testGetConfidencePairs(){
        HashMap<String, Integer> pair = new HashMap<String, Integer>() {
            {
                put("A B", 3);
                put("B C", 3);
            }
        };

        HashMap<String, Integer> single = new HashMap<String, Integer>(){
            {
                put("A", 10);
                put("B", 4);
                put("C", 10);
            }
        };

        bugDetector.pairSupport = pair;
        bugDetector.singleSupport = single;

        bugDetector.getConfidencePairs();

        HashMap<String, HashSet<String>> ans = new HashMap<String, HashSet<String>>() {
            {
                put("B", new HashSet<String>() {
                    {
                        add("A"); add("C");
                    }
                });
            }
        };

        Assert.assertEquals(ans, bugDetector.confidencePairs);
    }

    @Test
    public void testCalculateConfidence(){
        HashMap<String, Integer> pair = new HashMap<String, Integer>() {
            {
                put("A B", 20);
            }
        };

        HashMap<String, Integer> single = new HashMap<String, Integer>(){
            {
                put("A", 25);
            }
        };

        bugDetector.pairSupport = pair;
        bugDetector.singleSupport = single;
        Assert.assertEquals(80, bugDetector.calculateConfidence("A", "A B"), 0.01);
        Assert.assertEquals(-1, bugDetector.calculateConfidence("B", "A aB"), 0.01);
    }

    @Test
    public void testGetSupportForOneFunctionCall(){
        HashSet<String> set = new HashSet<String>() {
            {add("UchidaMaaya"); add("SakuraAyane"); add("MinaseInori");}
        };
        bugDetector.getSupportsForOneFuncCall(set);

        HashMap<String, Integer> single = new HashMap<String, Integer>() {
            {
                put("UchidaMaaya", 1);put("SakuraAyane", 1);put("MinaseInori", 1);
            }
        };
        HashMap<String, Integer> pair = new HashMap<String, Integer>() {
            {
                put("SakuraAyane UchidaMaaya", 1);put("MinaseInori UchidaMaaya", 1);put("MinaseInori SakuraAyane", 1);
            }
        };
        Assert.assertEquals(single, bugDetector.singleSupport);
        Assert.assertEquals(pair, bugDetector.pairSupport);
        set = new HashSet<String>() {
            {add("UchidaMaaya"); add("MinaseInori");}
        };
        bugDetector.getSupportsForOneFuncCall(set);

        single.put("UchidaMaaya", 2);
        single.put("MinaseInori", 2);
        pair.put("MinaseInori UchidaMaaya", 2);
        Assert.assertEquals(single, bugDetector.singleSupport);
        Assert.assertEquals(pair, bugDetector.pairSupport);
    }

    @Test
    public void testGetSupports(){
        bugDetector.getSupports(options.get(Main.CALLGRAPH));

        //Call Graph:
        HashMap<String, HashSet<String>> ans = new HashMap<String, HashSet<String>>() {
            {
                put("scope1", new HashSet<String>(){
                    {add("A"); add("B"); add("C"); add("D");}
                });

                put("scope2", new HashSet<String>(){
                    {add("A"); add("C"); add("D");}
                });
            }
        };

        HashMap<String, Integer> singleSupport = new HashMap<String, Integer>() {
            {
                put("A", 2); put("B", 1); put("C", 2); put("D", 2);
            }
        };

        Assert.assertEquals(ans, bugDetector.callGraph);
        Assert.assertEquals(singleSupport, bugDetector.singleSupport);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSupportsException(){
        bugDetector.getSupports("src/input/non-writable-output");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInferBugException(){
        bugDetector.options.put(Main.OUTPUT, "src/input/non-writable-output");
        bugDetector.inferBugs();
    }
}
