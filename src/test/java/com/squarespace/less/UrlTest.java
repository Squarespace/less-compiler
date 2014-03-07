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

import static com.squarespace.less.parse.Parselets.FUNCTION_CALL;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;


public class UrlTest extends LessTestBase {

  @Test
  public void testEquals() {
    assertEquals(url(quoted('"', false, "http://foo.com")), url(quoted('"', false, "http://foo.com")));

    assertNotEquals(url(quoted('"', false, "http://foo.com")), null);
    assertNotEquals(url(quoted('"', false, "http://foo.com")), anon("http://foo.com"));
    assertNotEquals(url(quoted('"', false, "http://foo.com")), url(quoted('"', false, "http://bar.com")));
  }

  @Test
  public void testModelReprSafety() {
    url(quoted('"', false, "http://squarespace.com/@{page}")).toString();
  }

  @Test
  public void testUrl() throws LessException {
    LessHarness h = new LessHarness(FUNCTION_CALL);

    h.parseEquals("url('http://foo.com/@{bar}')",
        url(quoted('\'', false, anon("http://foo.com/"), var("@bar", true))));

    h.parseEquals("url(http://glonk.com)",
        url(anon("http://glonk.com")));
  }

}
