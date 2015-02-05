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

package com.squarespace.less;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class AnoymousTest extends LessTestBase {

  @Test
  public void testEquals() throws LessException {
    assertEquals(anon(), anon());
    assertEquals(anon("foo"), anon("foo"));

    assertNotEquals(anon("foo"), null);
    assertNotEquals(anon("foo"), anon("bar"));
  }

  @Test
  public void testModelReprSafety() {
    anon("x").toString();
  }

}
