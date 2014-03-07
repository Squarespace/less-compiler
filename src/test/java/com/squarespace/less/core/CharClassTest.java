package com.squarespace.less.core;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class CharClassTest {

  @Test
  public void testClasses() {
    for (int i = 0; i < 10; i++) {
      char ch = (char)('0' + i);
      assertTrue(CharClass.digit(ch));
      assertFalse(CharClass.combinator(ch));
      assertFalse(CharClass.uppercase(ch));
    }

    for (int i = 0; i < 26; i++) {
      char upper = (char)('A' + i);
      assertTrue(CharClass.uppercase(upper));

      char lower = (char)('a' + i);
      assertFalse(CharClass.uppercase(lower));
      assertFalse(CharClass.digit(upper));
      assertFalse(CharClass.digit(lower));
      assertFalse(CharClass.combinator(lower));
      assertFalse(CharClass.combinator(upper));
    }
  }

}
