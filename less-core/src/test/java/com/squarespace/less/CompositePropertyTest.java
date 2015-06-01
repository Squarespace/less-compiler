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

import static com.squarespace.less.model.PropertyMergeMode.COMMA;
import static com.squarespace.less.model.PropertyMergeMode.SPACE;
import static com.squarespace.less.parse.Parselets.RULE_PROPERTY;
import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.CompositeProperty;


public class CompositePropertyTest extends LessTestBase {

  @Test
  public void testEquals() throws LessException {
    assertEquals(prop(list(prop("x"), var("@y"))), prop(list(prop("x"), var("@y"))));
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(RULE_PROPERTY);

    Node expected = prop(list(prop("foo-"), var("@var1", true), prop("-bar-"), var("@var2", true)));
    h.parseEquals("foo-@{var1}-bar-@{var2}  :", expected);
  }

  @Test
  public void testRepr() throws LessException {
    CompositeProperty actual = prop(list(prop("x-"), var("@y", true)), SPACE);
    assertEquals(actual.repr(), "x-@{y}+_");

    actual.mergeMode(COMMA);
    assertEquals(actual.repr(), "x-@{y}+");
  }

}
