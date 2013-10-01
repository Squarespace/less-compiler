package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.Combinator.CHILD;
import static com.squarespace.v6.template.less.model.Combinator.DESC;
import static com.squarespace.v6.template.less.model.Combinator.SIB_ADJ;
import static com.squarespace.v6.template.less.model.Combinator.SIB_GEN;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.Unit;


public class ValueRendererTest extends LessTestBase {

  @Test
  public void testComment() throws LessException {
    assertEquals(render(comment(" foo * // * bar ", true)), "/* foo * // * bar */");
    assertEquals(render(comment(" bar /* x */ baz", false)), "// bar /* x */ baz");
  }
  
  @Test
  public void testDimension() throws LessException {
    assertEquals(render(dim(12, Unit.PX)), "12px");
    assertEquals(render(dim(3.14, Unit.IN)), "3.14in");
    assertEquals(render(dim(2.1819)), "2.1819");
  }
  
  @Test
  public void testParenthesis() throws LessException {
    assertEquals(render(paren(kwd("foo"))), "(foo)");
    assertEquals(render(paren(dim(12, Unit.CM))), "(12cm)");
  }

  @Test
  public void testQuoted() throws LessException {
    assertEquals(render(quoted('"', "foo", "\"bar")), "\"foo\"bar\"");
    assertEquals(render(quoted('\'', "bar")), "'bar'");
    assertEquals(render(quoted(Chars.NULL, "bar", "foo")), "barfoo");
  }
  
  @Test
  public void testSelector() throws LessException {
    List<Selector> selectors = Arrays.asList(
        selector(element(CHILD, "span"), element(DESC, "b")),
        selector(element("p"), element(SIB_ADJ, "div"), element(CHILD, "span")),
        selector(element("p"), element(null, ".class-1"), element(null, ".class-2")),
        selector(element("ul"), element("li"), element(null, ".one"), element(SIB_GEN, "li"), element(null, ".two")),
        selector(element("a"), element(null, ".b"), element("c"), element(CHILD, "d"))
        );
    
    List<String> normal = Arrays.asList(
        "> span b",
        "p + div > span",
        "p.class-1.class-2",
        "ul li.one ~ li.two",
        "a.b c > d"
        );
    
    List<String> compressed = Arrays.asList(
        ">span b",
        "p+div>span",
        "p.class-1.class-2",
        "ul li.one~li.two",
        "a.b c>d"
        );
    for (int i = 0; i < selectors.size(); i++) {
      assertEquals(render(selectors.get(i)), normal.get(i));
      assertEquals(compress(selectors.get(i)), compressed.get(i));
    }
  }

  private String render(Node node) throws LessException {
    return render(node, false);
  }
  
  private String compress(Node node) throws LessException {
    return render(node, true);
  }

  private String render(Node node, boolean compress) throws LessException {
    Context ctx = new Context(new Options(compress));
    ExecEnv env = ctx.newEnv();
    return env.context().render(node);
  }
  
  
  
}