/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
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
