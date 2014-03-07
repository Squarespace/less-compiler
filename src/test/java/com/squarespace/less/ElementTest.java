package com.squarespace.less;

import static com.squarespace.less.model.Combinator.CHILD;
import static com.squarespace.less.model.Combinator.DESC;
import static com.squarespace.less.model.Combinator.SIB_ADJ;
import static com.squarespace.less.model.Combinator.SIB_GEN;

import org.testng.annotations.Test;

import com.squarespace.less.LessException;
import com.squarespace.less.core.LessHarness;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.parse.Parselets;


public class ElementTest extends LessTestBase {

  @Test
  public void testElement() throws LessException {
    LessHarness h = new LessHarness(Parselets.ELEMENT);

    h.parseEquals("h1", element(DESC, "h1"));
    h.parseEquals(" h1", element(DESC, "h1"));
    h.parseEquals("*", element("*"));
    h.parseEquals(">foo", element(CHILD, "foo"));
    h.parseEquals("+:hover", element(SIB_ADJ, ":hover"));
    h.parseEquals("~.class-one", element(SIB_GEN, ".class-one"));
    h.parseEquals(">&", element(CHILD, "&"));

    // Attribute
    h.parseEquals("[foo]", attr(DESC, anon("foo")));
    h.parseEquals("[foo~=\"bar\"]", attr(DESC, anon("foo"), anon("~="), quoted('"', false, "bar")));

    // Variable
    h.parseEquals("@{a}", varelem(DESC, var("@a", true)));
    h.parseEquals(">@{foo}", varelem(CHILD, var("@foo", true)));
  }

}
