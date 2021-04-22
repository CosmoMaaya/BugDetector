package se465;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RunWith(JUnit4.class)
public class MainTest {

  private Main o;

  @Before
  public void setup() throws IOException {
    o = new Main();

    //clear up output
    OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream("src/input/sample.out"), StandardCharsets.UTF_8);
    BufferedWriter bw = new BufferedWriter(outputStreamWriter);
    bw.close();
  }



  @Test
  public void  testMain(){
    String[] commands = new String[] {"--confidence", "50", "--support", "1", "--callgraph", "src/input/testCallGraph.txt", "--output", "src/input/sample.out"};
    Main.main(commands);

    HashSet<String> lines = new HashSet<>();
    try {
      InputStreamReader isr = new InputStreamReader(new FileInputStream(Main.options.get(Main.OUTPUT)), StandardCharsets.US_ASCII);
      BufferedReader br = new BufferedReader(isr);

      for (String line; (line = br.readLine()) != null; ) {
        lines.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    HashSet<String> expected = new HashSet<String>() {
      {
        add("bug: A in scope2, pair: (A, B), support: 1, confidence: 50.00%");
        add("bug: C in scope2, pair: (B, C), support: 1, confidence: 50.00%");
        add("bug: D in scope2, pair: (B, D), support: 1, confidence: 50.00%");
      }
    };
    Assert.assertEquals(expected, lines);
  }

  // Command parser test
  /* add your test code here */
  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserErrorFlag() {
    String[] commands = new String[] {"--unspported", "34"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserInvalidInput() {
    String[] commands = new String[] {"--unspported"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserDuplicateFlag() {
    String[] commands = new String[] {"--confidence", "20", "--confidence", "4"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserMissingFlag() {
    String[] commands = new String[] {"--confidence", "20", "--confidence", "4"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserWrongSupportValue() {
    String[] commands = new String[] {"--confidence", "20", "--support", "wring", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserNonPositiveSupportValue() {
    String[] commands = new String[] {"--confidence", "20", "--support", "0", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserInvalidConfidenceValue() {
    String[] commands = new String[] {"--confidence", "invalid", "--support", "3", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserWrongConfidenceValue() {
    String[] commands = new String[] {"--confidence", "900", "--support", "3", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserNonExistingCallgraph() {
    String[] commands = new String[] {"--confidence", "65", "--support", "3", "--callgraph", "", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserDirectoryOutput() {
    String[] commands = new String[] {"--confidence", "65", "--support", "3", "--callgraph", "src/input/testCallGraph.txt", "--output", "src"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserNonWritable() {
    String[] commands = new String[] {"--confidence", "65", "--support", "3", "--callgraph", "src/input/testCallGraph.txt", "--output", "src/input/non-writable-output"};
    Main.commandParser(commands);
  }

  @Test
  public void testCommandParserTrue() {
    String[] commands = new String[] {"--confidence", "0", "--support", "3", "--callgraph", "src/input/testCallGraph.txt", "--output", "src/input/sample.out"};
    HashMap<String, String> ans = Main.commandParser(commands);

    HashMap<String, String> expected = new HashMap<String, String>() {
      {
        put(Main.CONFIDENCE, "0");
        put(Main.SUPPORT, "3");
        put(Main.OUTPUT, "src/input/sample.out");
        put(Main.CALLGRAPH, "src/input/testCallGraph.txt");
      }
    };

    Assert.assertEquals(expected, ans);

    commands = new String[] {"--confidence", "100", "--support", "3", "--callgraph", "src/input/testCallGraph.txt", "--output", "src/input/sample.out"};
    ans = Main.commandParser(commands);
    expected.put(Main.CONFIDENCE, "100");

    Assert.assertEquals(expected, ans);
  }
}
