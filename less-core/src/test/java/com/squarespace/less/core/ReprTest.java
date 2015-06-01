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

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Node;


public class ReprTest extends LessTestBase {

  @Test
  public void testQuoted() throws LessException {
    Buffer buf = new Buffer(2);

    quoted('\'', false, anon("foo"), var("@foo", true)).repr(buf);
    assertEquals(buf.toString(), "'foo@{foo}'");

    buf.reset();
    quoted('"', true, var("@@foo", true)).repr(buf);
    assertEquals(buf.toString(), "~\"@@{foo}\"");
  }

  @Test
  public void testCompressed() throws LessException {
    LessHarness h = new LessHarness();
    Node sheet = h.parse(".p{color:#f00;font-size:1px;.c, .d{color:grey;font-size:2px;}color:green}");
    assertEquals(compressRepr(sheet), ".p{color:red;font-size:1px;.c,.d{color:grey;font-size:2px}color:green}");
    sheet = h.parse(".m(@a, @b) when (@a > 1) { color:#f00; }");
    assertEquals(compressRepr(sheet), ".m(@a, @b) when (@a > 1){color:red}");
  }

  private String compressRepr(Node node) {
    Buffer buf = new Buffer(4, true);
    node.repr(buf);
    return buf.toString();
  }

}
