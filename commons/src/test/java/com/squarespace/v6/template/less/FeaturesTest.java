package com.squarespace.v6.template.less;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessHarness;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Keyword;
import com.squarespace.v6.template.less.model.Paren;
import com.squarespace.v6.template.less.model.Variable;
import com.squarespace.v6.template.less.parse.Parselets;


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

    Paren rule_ab = paren(rule(prop("a"), kb));
    h.parseEquals("(a: b)", features(expn(rule_ab)));

    Paren rule_avb = paren(rule(prop("a"), vb));
    h.parseEquals("(a: @b)", features(expn(rule_avb)));
    h.parseEquals("(a: @b) and b", features(expn(rule_avb, and, kb)));
    h.parseEquals("(a: b), (a: @b)", features(expn(rule_ab), expn(rule_avb)));
  }
  
}
