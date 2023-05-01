package com.squarespace.less;

import org.testng.Assert;
import org.testng.annotations.Test;

public class Log2IntegerHistogramTest {

  @Test
  public void testBasic() {
    Log2IntegerHistogram hist = new Log2IntegerHistogram();
    hist.add(64);
    hist.add(128);
    hist.add(32);
    hist.add(0);
    hist.add(512);
    hist.add(256);
    hist.add(0);
    hist.add(128); // dupe
    Assert.assertEquals(hist.toString(), ",,,,,1,1,2,1,1");
  }

  @Test
  public void testEmpty() {
    Log2IntegerHistogram hist = new Log2IntegerHistogram();
    Assert.assertEquals(hist.toString(), "");
  }

  @Test
  public void testMerge() {
    Log2IntegerHistogram hist1 = new Log2IntegerHistogram();
    hist1.add(64);
    Log2IntegerHistogram hist2 = new Log2IntegerHistogram();
    hist2.add(128);

    hist1.merge(hist2);
    Assert.assertEquals(hist1.toString(), ",,,,,,1,1");
  }

  @Test
  public void testReset() {
    Log2IntegerHistogram hist = new Log2IntegerHistogram();
    hist.add(64);
    Assert.assertEquals(hist.toString(), ",,,,,,1");

    hist.reset();
    hist.add(512);
    Assert.assertEquals(hist.toString(), ",,,,,,,,,1");
  }
}
