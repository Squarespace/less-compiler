package com.squarespace.less.core;

import static com.squarespace.less.core.LessUtils.enumValueList;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;

import org.testng.annotations.Test;

public class LessUtilsTest {

  @Test
  public void testStripToNull() {
    assertEquals(LessUtils.stripToNull(""), null);
    assertEquals(LessUtils.stripToNull(" \t \n "), null);
    assertEquals(LessUtils.stripToNull("  \t foo \n bar  "), "foo \n bar");
  }

  @Test
  public void testStrip() {
    assertEquals(LessUtils.strip("", 0, 0), "");
    assertEquals(LessUtils.strip(" \t \n ", 0, 4), "");
    assertEquals(LessUtils.strip("  \t foo \n bar  ", 0, 15), "foo \n bar");
  }

  @Test
  public void testSplit() {
    assertEquals(LessUtils.split("", ':'), Arrays.asList());
    assertEquals(LessUtils.split(":", ':'), Arrays.asList("", ""));
    assertEquals(LessUtils.split("a", ':'), Arrays.asList("a"));
    assertEquals(LessUtils.split("a:b", ':'), Arrays.asList("a", "b"));
    assertEquals(LessUtils.split("a:b:c", ':'), Arrays.asList("a", "b", "c"));
  }

  @Test
  public void testRepeat() {
    assertEquals(LessUtils.repeat('a', 0), "");
    assertEquals(LessUtils.repeat('a', 1), "a");
    assertEquals(LessUtils.repeat('a', 10), "aaaaaaaaaa");
  }

  @Test
  public void testEscapeJava() {
    assertEquals(LessUtils.escapeJava("a b c"), "a b c");
    assertEquals(LessUtils.escapeJava("\"a b c\""), "\\\"a b c\\\"");
    assertEquals(LessUtils.escapeJava("\u2018foo\u2019"), "\\u2018foo\\u2019");
    assertEquals(LessUtils.escapeJava("\t\n\r\b\f\\"), "\\t\\n\\r\\b\\f\\\\");
    assertEquals(LessUtils.escapeJava("\u0000\u0003"), "\\u0000\\u0003");
  }

  @Test
  public void testEnumValueList() {
    assertEquals(enumValueList(ProductType.class, false), "UNDEFINED, PHYSICAL, DIGITAL, SERVICE, GIFT_CARD");
    assertEquals(enumValueList(ProductType.class, true), "undefined, physical, digital, service, gift_card");
  }

  private enum ProductType {
    UNDEFINED,
    PHYSICAL,
    DIGITAL,
    SERVICE,
    GIFT_CARD
  }

}
