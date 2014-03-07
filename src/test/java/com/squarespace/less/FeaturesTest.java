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
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.Variable;
import com.squarespace.less.parse.Parselets;


public class FeaturesTest extends LessTestBase {

  @Test
  public void testFeatures() throws LessException {
    LessHarness h = new LessHarness(Parselets.FEATURES);

    Keyword and = kwd("and");
    Keyword ka = kwd("a");
    Keyword kb = kwd("b");
    Keyword kc = kwd("c");
    h.parseEquals("a, b and c", features(expn(ka), expn(kb, and, kc)));

    Variable va = var("@a");
    Variable vb = var("@b");
    h.parseEquals("@a, @b", features(va, vb));

    Paren ruleAB = paren(rule(prop("a"), kb));
    h.parseEquals("(a: b)", features(expn(ruleAB)));

    Paren ruleAVB = paren(rule(prop("a"), vb));
    h.parseEquals("(a: @b)", features(expn(ruleAVB)));
    h.parseEquals("(a: @b) and b", features(expn(ruleAVB, and, kb)));
    h.parseEquals("(a: b), (a: @b)", features(expn(ruleAB), expn(ruleAVB)));

    h.parseEquals("handheld", features(expn(kwd("handheld"))));
    h.parseEquals("(screen)", features(expn(paren(kwd("screen")))));

    h.parseEquals("(min-width: @width)",
        features(expn(paren(rule(prop("min-width"), var("@width"))))));

    h.parseEquals("a and (b: 12px) and c",
        features(expn(kwd("a"), kwd("and"), paren(rule(prop("b"), dim(12, Unit.PX))), kwd("and"), kwd("c"))));
  }

}
