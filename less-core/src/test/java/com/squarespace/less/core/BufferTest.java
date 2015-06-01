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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;


public class BufferTest {

  @Test
  public void testEscaping() {
    Buffer buf = new Buffer(0, true);
    buf.startDelim('"');
    assertTrue(buf.inEscape());
    buf.append("\"foo\"");
    buf.endDelim();
    assertEquals(buf.toString(), "\"foo\"");
  }

  @Test
  public void testNullEscaping() {
    Buffer buf = new Buffer(0, true);
    buf.startDelim(Chars.NULL);
    assertTrue(buf.inEscape());
    buf.append("foo");
    buf.endDelim();
    assertFalse(buf.inEscape());
    assertEquals(buf.toString(), "foo");
  }

}
