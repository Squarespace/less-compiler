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

import org.testng.Assert;
import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;


public class RGBColorTest extends LessTestBase {

  @Test
  public void testEquals() {
    Assert.assertEquals(rgb(32, 32, 32), rgb(32, 32, 32));

    Assert.assertNotEquals(rgb(32, 32, 32), null);
    Assert.assertNotEquals(rgb(32, 32, 32), anon("foo"));
    Assert.assertNotEquals(rgb(32, 32, 32), rgb(1, 32, 32));
    Assert.assertNotEquals(rgb(32, 32, 32), rgb(32, 32, 32, 0.5));
  }

  @Test
  public void testModelReprSafety() {
    rgb(32, 32, 32, .7).toString();
  }

}
