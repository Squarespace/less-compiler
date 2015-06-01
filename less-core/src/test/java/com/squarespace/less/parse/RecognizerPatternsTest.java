/**
 * Copyright, 2015, Squarespace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.parse;

import static com.squarespace.less.parse.RecognizerPatterns.AND;
import static com.squarespace.less.parse.RecognizerPatterns.ATTRIBUTE_KEY;
import static com.squarespace.less.parse.RecognizerPatterns.ATTRIBUTE_OP;
import static com.squarespace.less.parse.RecognizerPatterns.CALL_NAME;
import static com.squarespace.less.parse.RecognizerPatterns.CONDITION_OP;
import static com.squarespace.less.parse.RecognizerPatterns.ELEMENT0;
import static com.squarespace.less.parse.RecognizerPatterns.ELEMENT1;
import static com.squarespace.less.parse.Recognizers.FAIL;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.parse.Recognizers.Recognizer;


public class RecognizerPatternsTest {

  @Test
  public void testAnd() {
    assertEquals(3, match(AND, "and"));
    assertEquals(3, match(AND, "andx"));

    assertEquals(FAIL, match(AND, "AND"));
  }

  @Test
  public void testAttributeKey() {
    assertEquals(1, match(ATTRIBUTE_KEY, "a"));
    assertEquals(1, match(ATTRIBUTE_KEY, "a..."));
    assertEquals(2, match(ATTRIBUTE_KEY, "\\."));
    assertEquals(13, match(ATTRIBUTE_KEY, "foo-bar\\.-baz"));
  }

  @Test
  public void testAttributeOp() {
    assertEquals(2, match(ATTRIBUTE_OP, "~="));
    assertEquals(2, match(ATTRIBUTE_OP, "*="));
    assertEquals(1, match(ATTRIBUTE_OP, "===="));

    assertEquals(FAIL, match(ATTRIBUTE_OP, "+="));
  }

  @Test
  public void testConditionOp() {
    assertEquals(2, match(CONDITION_OP, "<="));
    assertEquals(2, match(CONDITION_OP, ">="));
    assertEquals(2, match(CONDITION_OP, "=<"));
    assertEquals(2, match(CONDITION_OP, "=="));
    assertEquals(1, match(CONDITION_OP, "=~"));

    assertEquals(FAIL, match(CONDITION_OP, "~="));
  }

  @Test
  public void testCallName() {
    assertEquals(12, match(CALL_NAME, "abc-123_def("));
    assertEquals(14, match(CALL_NAME, "progid:foobar("));
    assertEquals(2, match(CALL_NAME, "%("));
  }

  @Test
  public void testElement0() {
    assertEquals(6, match(ELEMENT0, "1.234%"));
    assertEquals(2, match(ELEMENT0, "5%"));

    assertEquals(FAIL, match(ELEMENT0, "1.234"));
    assertEquals(FAIL, match(ELEMENT0, "1.%"));
    assertEquals(FAIL, match(ELEMENT0, "%"));
    assertEquals(FAIL, match(ELEMENT0, "abc%"));
  }

  @Test
  public void testElement1() {
    assertEquals(7, match(ELEMENT1, "#ns-1-2"));
    assertEquals(6, match(ELEMENT1, 0, ":hover"));
    assertEquals(10, match(ELEMENT1, 3, "___:extend"));
  }

  @Test
  public void testElement2() {
    assertEquals(3, match(RecognizerPatterns.ELEMENT2, "(a)"));
    assertEquals(5, match(RecognizerPatterns.ELEMENT2, "(foo)"));

    assertEquals(FAIL, match(RecognizerPatterns.ELEMENT2, "()"));
  }

  @Test
  public void testElement3() {
    assertEquals(1, match(RecognizerPatterns.ELEMENT3, ".@{var}"));
    assertEquals(1, match(RecognizerPatterns.ELEMENT3, "#@{var}"));

    assertEquals(FAIL, match(RecognizerPatterns.ELEMENT3, ".#@{var}"));
    assertEquals(FAIL, match(RecognizerPatterns.ELEMENT3, ".foo@{var}"));
  }

  @Test
  public void testDimensionValue() {
    assertEquals(1, match(RecognizerPatterns.DIMENSION_VALUE, "1"));
    assertEquals(2, match(RecognizerPatterns.DIMENSION_VALUE, "1."));
    assertEquals(3, match(RecognizerPatterns.DIMENSION_VALUE, "1.0"));
    assertEquals(2, match(RecognizerPatterns.DIMENSION_VALUE, ".1"));
    assertEquals(3, match(RecognizerPatterns.DIMENSION_VALUE, "0.1"));
    assertEquals(5, match(RecognizerPatterns.DIMENSION_VALUE, "11.22"));

    assertEquals(FAIL, match(RecognizerPatterns.DIMENSION_VALUE, "."));
    assertEquals(FAIL, match(RecognizerPatterns.DIMENSION_VALUE, ".x"));
    assertEquals(FAIL, match(RecognizerPatterns.DIMENSION_VALUE, "x."));
  }

  @Test
  public void testHexColor() {
    assertEquals(7, match(RecognizerPatterns.HEXCOLOR, "#000000"));
    assertEquals(7, match(RecognizerPatterns.HEXCOLOR, "#123456"));
    assertEquals(4, match(RecognizerPatterns.HEXCOLOR, "#123"));
    assertEquals(4, match(RecognizerPatterns.HEXCOLOR, "#1234"));
    assertEquals(7, match(RecognizerPatterns.HEXCOLOR, "#123456789"));

    assertEquals(FAIL, match(RecognizerPatterns.HEXCOLOR, "#"));
    assertEquals(FAIL, match(RecognizerPatterns.HEXCOLOR, "#1"));
    assertEquals(FAIL, match(RecognizerPatterns.HEXCOLOR, "#12"));
  }

  private int match(Recognizer pattern, String str) {
    return match(pattern, 0, str);
  }

  private int match(Recognizer pattern, int pos, String str) {
    return pattern.match(str, pos, str.length());
  }

}
