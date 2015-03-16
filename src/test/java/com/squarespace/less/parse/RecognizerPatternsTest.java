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

import static com.squarespace.less.parse.RecognizerPatterns.ATTRIBUTE_KEY;
import static com.squarespace.less.parse.RecognizerPatterns.ELEMENT0;
import static com.squarespace.less.parse.RecognizerPatterns.ELEMENT1;
import static com.squarespace.less.parse.Recognizers.FAIL;
import static junit.framework.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.parse.Recognizers.Recognizer;


public class RecognizerPatternsTest {

  @Test
  public void testAttributeKey() {
    assertEquals(1, match(ATTRIBUTE_KEY, "a"));
    assertEquals(2, match(ATTRIBUTE_KEY, "\\."));
    assertEquals(13, match(ATTRIBUTE_KEY, "foo-bar\\.-baz"));

    assertEquals(1, match(ATTRIBUTE_KEY, "a..."));
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

  private int match(Recognizer pattern, String str) {
    return match(pattern, 0, str);
  }

  private int match(Recognizer pattern, int pos, String str) {
    return pattern.match(str, pos, str.length());
  }

}
