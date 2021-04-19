package se465;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PairTest {
    /* add your test code here */
    @Test
    public void testEqual() {
        Pair pair1 = new Pair(1, 2);
        Pair pair2 = new Pair(2, 1);

        Assert.assertEquals(pair1, pair2);
    }

    @Test
    public void testEqualSelf(){
        Pair pair1 = new Pair(1, 2);
        Assert.assertEquals(pair1, pair1);
    }

    @Test
    public void testEqualDiffobj(){
        Pair pair1 = new Pair(1, 2);
        Object obj = new double[] {3};
        Assert.assertNotEquals(pair1, obj);
    }

    @Test
    public void testHashCode(){
        Pair pair = new Pair(1, 2);

        Assert.assertEquals(pair.hashCode(), 33);
    }
}
