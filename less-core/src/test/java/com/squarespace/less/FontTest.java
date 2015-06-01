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

import static com.squarespace.less.model.Operator.DIVIDE;
import static com.squarespace.less.model.Unit.PX;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Node;
import com.squarespace.less.parse.Parselets;


public class FontTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(Parselets.RULE);
    Node expected = rule(prop("font"), expn(oper(DIVIDE, dim(0), dim(0)), kwd("a")));
    h.parseEquals("font: 0/0 a", expected);

    expected = rule(prop("font"), expn(shorthand(kwd("small"), dim(0)), kwd("a")));
    h.parseEquals("font: small/0 a", expected);

    expected = rule(prop("font"), oper(DIVIDE, dim(12, PX), dim(14, PX)));
    h.parseEquals("font: 12px/14px", expected);

    expected = rule(prop("font"), oper(DIVIDE, dim(12, PX), dim(14, PX)));
    h.parseEquals("font: 12px / 14px", expected);

    expected = rule(prop("font"), expn(dim(400), oper(DIVIDE, dim(12, PX), dim(14, PX))));
    h.parseEquals("font: 400 12px / 14px", expected);
  }

}
