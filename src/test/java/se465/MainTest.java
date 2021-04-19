package se465;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.Map;

@RunWith(JUnit4.class)
public class MainTest {

  private Main o;

  @Before
  public void setup() {
    o = new Main();
  }





  // Command parser test
  /* add your test code here */
  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserErrorFlag() {
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
  public void testCommandParserNegativeSupportValue() {
    String[] commands = new String[] {"--confidence", "20", "--support", "-5", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserInvalidConfidenceValue() {
    String[] commands = new String[] {"--confidence", "invalid", "--support", "65", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserWrongConfidenceValue() {
    String[] commands = new String[] {"--confidence", "900", "--support", "65", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserNonExistingCallgraph() {
    String[] commands = new String[] {"--confidence", "3", "--support", "65", "--callgraph", "", "--output", "a.out"};
    Main.commandParser(commands);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCommandParserDirectoryOutput() {
    String[] commands = new String[] {"--confidence", "3", "--support", "65", "--callgraph", "src/input/testCallGraph.txt", "--output", "src"};
    Main.commandParser(commands);
  }
  @Test
  public void testCommandParserTrue() {
    String[] commands = new String[] {"--confidence", "3", "--support", "65", "--callgraph", "src/input/testCallGraph.txt", "--output", "a.out"};
    HashMap<String, String> ans = Main.commandParser(commands);
    Assert.assertEquals(ans.get(Main.CONFIDENCE), "3");
    Assert.assertEquals(ans.get(Main.SUPPORT), "65");
    Assert.assertEquals(ans.get(Main.OUTPUT), "a.out");
    Assert.assertEquals(ans.get(Main.CALLGRAPH), "src/input/testCallGraph.txt");
  }
}
