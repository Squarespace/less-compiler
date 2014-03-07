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
import com.squarespace.less.model.Parameter;


public class ParametersTest extends LessTestBase {

  @Test
  public void testEquals() {
    Parameter paramXY = param("@x", anon("y"));
    Parameter paramX = param("@x", true);

    assertEquals(params(), params());
    assertEquals(params(paramXY, paramX), params(paramXY, paramX));

    assertNotEquals(params(paramXY, paramX), null);
    assertNotEquals(params(paramXY, paramX), params(paramXY));
    assertNotEquals(paramXY, paramX);
  }

  @Test
  public void testModelReprSafety() {
    params(param("@x", anon("y")), param("@z", true)).toString();
  }

}
