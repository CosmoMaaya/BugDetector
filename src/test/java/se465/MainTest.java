package se465;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class MainTest {

  private Main o;

  @Before
  public void setup() {
    o = new Main();
  }

  /* add your test code here */
  @Test
  public void testAdd() {
    Assert.assertEquals(0, o.add(0, 0));
    Assert.assertEquals(4, o.add(1, 3));
  }
}
