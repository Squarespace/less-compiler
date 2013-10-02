package com.squarespace.v6.template.less;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.MixinMatcher;
import com.squarespace.v6.template.less.exec.MixinResolver;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinCallArgs;
import com.squarespace.v6.template.less.model.MixinParams;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Stylesheet;


public class MixinResolverTest extends LessTestBase {

  @Test
  public void testResolver() throws LessException {
    Context ctx = new Context();
    ExecEnv env = ctx.newEnv();
    MixinCall call = mixincall(selector(element("#ns"), element(".m1")));
    MixinMatcher matcher = new MixinMatcher(env, call);

    Stylesheet sheet = stylesheet();
    Ruleset r1 = ruleset(selector(element("#ns")));
    Ruleset r2 = ruleset(selector(element(".m1")));
    r2.add(rule(prop("color"), color("white")));
    r1.add(r2);
    sheet.add(r1);

    MixinResolver resolver = new MixinResolver();
    resolver.reset(matcher);

    System.out.println(resolver.match(sheet.block()));
  }
  
  @Test
  public void testBindSpeed() throws Exception {
    int N = 1000;
    System.out.println("waiting..");
    Context ctx = new Context();
    ExecEnv env = ctx.newEnv();
    MixinCallArgs args = args(',', arg("a", dim(1)), arg("b", dim(2)), arg("c", dim(3)), arg(dim(17)));
    MixinCall call = mixincall(selector(element(".mixin")), args);

    MixinParams params = params(param("a"), param("b"), param("c"), param("rest", true));
    MixinMatcher matcher = new MixinMatcher(env, call);
    
//    Thread.sleep(15000);
//    System.out.println("running..");
    for (int i = 0; i < N; i++) {
      matcher.bind(params);
    }
//    System.out.println("done.");
  }
  
}
