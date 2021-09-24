package com.squarespace.less.match;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Double-array trie tests.
 */
public class DatTest {

  @Test
  public void testBasic() {
    List<String> strs = Arrays.asList("OAR", "AT", "OAKS", "ANT", "OARS", "OATS", "OAT",
        "OARING", "ROARING", "ANTS", "ANTI", "PANT", "PAT", "RAT",
        "BANANA", "BAND", "BANDANA", "BANNIF", "B", "O", "A");
    DAT dat = build(strs, true);

    // Ensure all of the input strings match
    for (String s : strs) {
      assertEquals(strs.indexOf(s), dat.getIgnoreCase(s, 0, s.length()));
      assertEquals(strs.indexOf(s), dat.getIgnoreCase(s.toLowerCase(), 0, s.length()));
    }

    // partial matches fail
    assertEquals(-1, dat.getIgnoreCase("oak", 0, 3));
    assertEquals(-1, dat.get("OAKS", 0, 4));
  }

  @Test
  public void testDupes() {
    List<String> strs;

    strs = Arrays.asList("FOO", "BAR", "FOO");
    try {
      build(strs);
      fail("expected duplicate strings to raise an exception");
    } catch (IllegalArgumentException e) {
      // expected
    }

    strs = Arrays.asList("FOO", "BAR", "foo");
    build(strs);
    try {
      build(strs, true);
      fail("expected duplicate strings to raise an exception");
    } catch (IllegalArgumentException e) {
      // expected
    }
  }

  private DAT build(List<String> keys) {
    DATBuilder builder = new DATBuilder(keys);
    builder.build();
    return new DAT(builder.base(), builder.check(), builder.indices());
  }

  private DAT build(List<String> keys, boolean ignoreCase) {
    DATBuilder builder = new DATBuilder(keys, ignoreCase);
    builder.build();
    return new DAT(builder.base(), builder.check(), builder.indices());
  }

}
