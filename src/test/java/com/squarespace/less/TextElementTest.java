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

import static com.squarespace.less.model.Combinator.CHILD;
import static com.squarespace.less.model.Combinator.DESC;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class TextElementTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(element(".x"), element(".x"));
    assertEquals(element(null, ".x"), element(null, ".x"));
    assertEquals(element(CHILD, ".x"), element(CHILD, ".x"));

    assertNotEquals(element(".x"), null);
    assertNotEquals(element(".x"), kwd("foo"));
    assertNotEquals(element(".x"), element(".y"));
    assertNotEquals(element(CHILD, ".x"), element(DESC, ".y"));
    assertNotEquals(element(null, ".x"), element(DESC, ".x"));
  }

  @Test
  public void testModelReprSafety() {
    element(null, ".foo").toString();
    element(CHILD, ".foo").toString();
  }

}
