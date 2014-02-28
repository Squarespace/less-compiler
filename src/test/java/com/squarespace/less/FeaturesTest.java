package com.squarespace.less;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
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

    Paren rule_ab = paren(rule(prop("a"), kb));
    h.parseEquals("(a: b)", features(expn(rule_ab)));

    Paren rule_avb = paren(rule(prop("a"), vb));
    h.parseEquals("(a: @b)", features(expn(rule_avb)));
    h.parseEquals("(a: @b) and b", features(expn(rule_avb, and, kb)));
    h.parseEquals("(a: b), (a: @b)", features(expn(rule_ab), expn(rule_avb)));

    h.parseEquals("handheld", features(expn(kwd("handheld"))));
    h.parseEquals("(screen)", features(expn(paren(kwd("screen")))));
    
    h.parseEquals("(min-width: @width)", 
        features(expn(paren(rule(prop("min-width"), var("@width"))))));

    h.parseEquals("a and (b: 12px) and c",
        features(expn(kwd("a"), kwd("and"), paren(rule(prop("b"), dim(12, Unit.PX))), kwd("and"), kwd("c"))));
  }
  
}
