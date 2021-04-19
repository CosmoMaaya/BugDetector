package se465;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class BugDetectorTest {

    private HashMap<String, String> options;
    private BugDetector bugDetector;

    @Before
    public void setUp(){
        options = new HashMap<>();
        options.put(Main.SUPPORT, "3");
        options.put(Main.CONFIDENCE, "65");
        options.put(Main.OUTPUT, "a.out");
        options.put(Main.CALLGRAPH, "src/input/testCallGraph.txt");

        bugDetector = new BugDetector(options);
    }

    @Test
    public void testGetSupportForOneFunctionCall(){
        HashSet<Integer> set = new HashSet<Integer>() {
            {add(2); add(3); add(4); add(5);}
        };

        bugDetector.getSupportsForOneFuncCall(set);

        for (int i = 2; i <= 5; i++){
            int support = bugDetector.singleSupport.get(i);
            Assert.assertEquals(support, 1);
        }

        for (int i = 2; i <= 4; i++){
            for (int j = i + 1; j <= 5; j++){
                Pair pair = new Pair(i, j);
                int support = bugDetector.pairSupport.get(pair);
                Assert.assertEquals();
            }
        }

    }

    @Test
    public void testGetSupports(){
        bugDetector.getSupports(options.get(Main.CALLGRAPH));

        HashMap<String, Integer> funcToID = bugDetector.funcToID;
        HashMap<Integer, String> IDToFunc= bugDetector.iDToFunc;

        Assert.assertEquals(funcToID.size(), IDToFunc.size());

        //Func to ID
        int id = funcToID.get("scope1");
        Assert.assertEquals(id, 0);
        id = funcToID.get("scope2");
        Assert.assertEquals(id, 1);
        id = funcToID.get("A");
        Assert.assertEquals(id, 2);
        id = funcToID.get("B");
        Assert.assertEquals(id, 3);
        id = funcToID.get("C");
        Assert.assertEquals(id, 4);
        id = funcToID.get("D");
        Assert.assertEquals(id, 5);

        for (Map.Entry<String, Integer> entry: funcToID.entrySet()){
            id = entry.getValue();
            String func = IDToFunc.get(id);
            Assert.assertEquals(func, entry.getKey());
        }

        //Call Graph:
        HashSet<Integer> set = new HashSet<Integer>() {
            //A,B,C,D
            {add(2); add(3); add(4); add(5);}
        };
        Assert.assertEquals(set, bugDetector.callGraph.get(0));
        set = new HashSet<Integer>() {
            //A,C,D
            {add(2); add(4); add(5);}
        };
        Assert.assertEquals(set, bugDetector.callGraph.get(1));

        Assert.assertNull(bugDetector.callGraph.get(2));
        Assert.assertNull(bugDetector.callGraph.get(3));
        Assert.assertNull(bugDetector.callGraph.get(4));
        Assert.assertNull(bugDetector.callGraph.get(5));

    }
}
