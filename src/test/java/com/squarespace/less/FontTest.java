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

import static com.squarespace.less.model.Unit.PX;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse2.LessSyntax;


public class FontTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.FONT);
    h.parseEquals("0/0 a", expnlist(expn(ratio("0/0"), kwd("a"))));
    h.parseEquals("small/0 a", expnlist(expn(shorthand(kwd("small"), dim(0)), kwd("a"))));

    h.parseEquals("12px/14px", expnlist(expn(shorthand(dim(12, PX), dim(14, PX)))));
    h.parseEquals("12px / 14px", expnlist(expn(dim(12, PX), anon("/"), dim(14, PX))));
    h.parseEquals("400 12px / 14px", expnlist(expn(dim(400), dim(12, PX), anon("/"), dim(14, PX))));
  }

}
