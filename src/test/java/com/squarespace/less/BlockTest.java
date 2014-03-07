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
import com.squarespace.less.model.Rule;


public class BlockTest extends LessTestBase {

  @Test
  public void testEquals() {
    Rule ruleXY = rule(prop("x"), anon("y"));
    Rule ruleXZ = rule(prop("x"), anon("z"));
    assertEquals(block(ruleXY, ruleXZ), block(ruleXY, ruleXZ));

    assertNotEquals(block(ruleXY), block());
    assertNotEquals(block(), block(ruleXY));
    assertNotEquals(block(ruleXY, ruleXZ), block(ruleXZ, ruleXY));
  }

  @Test
  public void testModelReprSafety() {
    block(rule(prop("x"), anon("y"))).toString();
    block(rule(prop("x"), anon("y"))).toString();
  }

}
