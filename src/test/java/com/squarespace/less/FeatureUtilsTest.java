package com.squarespace.less;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.FeatureUtils;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.Variable;


public class FeatureUtilsTest extends LessTestBase {

  @Test
  public void testCombinations() throws LessException {
    Keyword and = kwd("and");
    Keyword ka = kwd("a");
    Keyword kb = kwd("b");
    Keyword kc = kwd("c");
    Keyword kd = kwd("d");
    Variable va = var("@a");
    Paren foo = paren(rule(prop("foo"), va));
    Paren bar = paren(rule(prop("bar"), dim(12, Unit.PX)));

    // IN: a b { c {
    Features ancestors = features(expn(ka, kb));
    Features current = features(expn(kc));
    assertEquals(render(ancestors, current), "a b and c");

    // IN: a, b { c {
    ancestors = features(expn(ka), expn(kb));
    assertEquals(render(ancestors, current), "a and c, b and c");

    // IN: a, b { c, d {
    current = features(expn(kc), expn(kd));
    assertEquals(render(ancestors, current), "a and c, b and c, a and d, b and d");

    // IN: a and b, a { c, d {
    ancestors = features(expn(ka, and, kb), expn(ka));
    assertEquals(render(ancestors, current), "a and b and c, a and c, a and b and d, a and d");

    GenericBlock defs = defs(
        def("@a", anon("xyz"))
    );

    // IN: @a { c, d {
    ancestors = features(expn(va));
    assertEquals(render(ancestors, current, defs), "xyz and c, xyz and d");

    // IN: (foo: @a) { c, (bar: 12px) {
    ancestors = features(expn(foo));
    current = features(expn(kc), expn(bar));
    assertEquals(render(ancestors, current, defs), "(foo: xyz) and c, (foo: xyz) and (bar: 12px)");
  }

  private String render(Features ancestors, Features current) throws LessException {
    return render(ancestors, current, null);
  }

  private String render(Features ancestors, Features current, GenericBlock defs) throws LessException {
    LessHarness h = new LessHarness();
    Context ctx = h.context();
    ExecEnv env = ctx.newEnv();
    if (defs != null) {
      env.push(defs);
    }
    Features result = FeatureUtils.combine((Features)ancestors.eval(env), (Features)current.eval(env));
    return env.context().render(result);
  }

}
