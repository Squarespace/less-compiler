package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.CHILD;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.model.Mixin;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinParams;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.Stylesheet;


public class MixinLookupTest extends LessTestBase {

  @Test
  public void testBasic() throws LessException {
    
  }
  
  @Test
  public void testFIXME() throws LessException {
    Selector s0 = selector(element(".foo"));
    Selector s1 = selector(element(".baz"));
    
    Ruleset r0 = ruleset();
    Ruleset r1 = ruleset(s0);
    Mixin r2 = new Mixin(".bar", new MixinParams(), null);
    Ruleset r3 = ruleset(s1);
    
    r3.add(rule(prop("color"), color("#123"), false));
    
    r0.add(r1);
    r1.add(r2);
    r2.add(r3);
    
    MixinCall m0 = new MixinCall(selector(element(CHILD, ".bar")), null, false);
    r1.add(m0);
    
    Stylesheet s = sheet();
    s.block().append(r0);

//    CssRenderer cr = new CssRenderer(new Context(new Options()));
//    
//    System.out.println(cr.render(s));
//    
//    LessEnv env = new LessEnv();
//    r0.toCSS(env);
//    System.out.println(env.buffer().toString());
  }
  
}
