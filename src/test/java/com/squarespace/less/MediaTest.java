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

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.Parselets;


public class MediaTest extends LessTestBase {

  @Test
  public void testEquals() {
    Media m0 = media();
    Media m1 = media(features(kwd("screen")));
    Media m2 = media(features(kwd("mobile")));

    assertEquals(m0, media());
    assertEquals(m1, media(features(kwd("screen"))));

    assertNotEquals(m0, m1);
    assertNotEquals(m1, m2);
  }

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.DIRECTIVE);

    Node exp = media(features(expn(kwd("screen"), kwd("and"), kwd("mobile"))));
    h.parseEquals("@media screen and mobile { }", exp);
  }

}
