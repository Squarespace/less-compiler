package com.squarespace.less;

import static com.squarespace.less.model.Combinator.DESC;
import static com.squarespace.less.model.Combinator.SIB_ADJ;
import static org.testng.Assert.assertEquals;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.SelectorUtils;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.TextElement;


public class SelectorUtilsTest extends LessTestBase {

  @Test
  public void testNested() {
    TextElement parent = element(".parent");
    TextElement child = element(DESC, ".child");
    TextElement sibling = element(DESC, ".sibling");
    
    Selectors ancestors = selectors(selector(parent));
    Selectors current = selectors(selector(child), selector(sibling));
    Selectors result = SelectorUtils.combine(ancestors, current);

    List<Selector> actual = result.selectors();
    assertEquals(actual.size(), 2);
    assertEquals(actual.get(0), selector(parent, child));
    assertEquals(actual.get(1), selector(parent, sibling));
  }
  
  @Test
  public void testWildcard() {
    TextElement parent = element(".parent");
    TextElement child = element(".child");
    TextElement sibling = element(".sibling");
    TextElement wild = element(DESC, "&");

    Selectors ancestors = selectors(selector(child), selector(sibling));
    Selectors current = selectors(selector(parent, wild));
    Selectors result = SelectorUtils.combine(ancestors, current);
    
    List<Selector> actual = result.selectors();
    assertEquals(actual.size(), 2);
    assertEquals(actual.get(0), selector(parent, wild, child));
    assertEquals(actual.get(1), selector(parent, wild, sibling));
  }
  
  @Test
  public void testMultipleWildcards() {
    TextElement child = element(".child");
    TextElement sibling = element(".sibling");
    TextElement wild1 = element("&");
    TextElement wild2 = element(SIB_ADJ, "&");

    Selectors ancestors = selectors(selector(child), selector(sibling));
    Selectors current = selectors(selector(wild1, wild2));
    Selectors result = SelectorUtils.combine(ancestors, current);
    
    List<Selector> actual = result.selectors();
    assertEquals(actual.size(), 4);
    assertEquals(actual.get(0), selector(wild1, child, wild2, child));
    assertEquals(actual.get(1), selector(wild1, child, wild2, sibling));
    assertEquals(actual.get(2), selector(wild1, sibling, wild2, child));
    assertEquals(actual.get(3), selector(wild1, sibling, wild2, sibling));
  }
  
}
