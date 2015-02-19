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

import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.Constants.TRUE;
import static com.squarespace.less.model.Operator.AND;
import static com.squarespace.less.model.Operator.EQUAL;

import org.testng.annotations.Test;

import com.squarespace.less.core.Constants;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.parse.Parselets;


public class GuardTest extends LessTestBase {

  @Test
  public void testParse() throws LessException {
    Dimension one = dim(1);
    Dimension two = dim(2);
    Condition oneEq1 = cond(EQUAL, one, one);

    // NOTE: the NOT_EQUAL operator is disabled until it can be added
    // upstream to less.js or enabled as an extension.

//    Condition twoEq2 = cond(EQUAL, two, two);
//    Condition oneNe2 = cond(NOT_EQUAL, one, two);

    LessHarness h = new LessHarness(Parselets.MIXIN_GUARD);

    h.parseEquals("when (1)",
        guard(cond(EQUAL, one, Constants.TRUE)));

//    h.parseEquals("when (1=1), (2=2) and (1!=2)",
//        guard(oneEq1, cond(AND, twoEq2, oneNe2)));

    h.parseEquals("when not (1=1)",
        guard(cond(EQUAL, one, one, true)));

    h.parseEquals("when (1=1) and not (2=2)",
        guard(cond(AND, oneEq1, cond(EQUAL, two, two, true))));

    h.parseEquals("when (true), (false)",
        guard(cond(EQUAL, TRUE, TRUE), cond(EQUAL, FALSE, TRUE)));

    h.parseEquals("when(true),(false)",
        guard(cond(EQUAL, TRUE, TRUE), cond(EQUAL, FALSE, TRUE)));
  }

}
