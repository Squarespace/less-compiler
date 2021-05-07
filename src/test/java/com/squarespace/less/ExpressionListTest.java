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

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.LessSyntax;


public class ExpressionListTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    LessHarness h = new LessHarness(LessSyntax.EXPRESSION_LIST);

    h.parseEquals("1, 2, 3", expnlist(dim(1), dim(2), dim(3)));
    h.parseEquals("a b, c d", expnlist(expn(kwd("a"), kwd("b")), expn(kwd("c"), kwd("d"))));
  }

}
