package com.squarespace.v6.template.less.model;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.exec.ExecEnv;


/**
 * Represents a comma-separated list of Selector nodes that forms
 * the header of a Ruleset.
 */
public class Selectors extends BaseNode {

  private List<Selector> selectors;

  private boolean evaluate;
  
  public Selectors() {
  }
  
  public Selectors(List<Selector> selectors) {
    this.selectors = selectors;
  }
  
  public boolean isEmpty() {
    return selectors == null ? true : selectors.isEmpty();
  }
  
  public void add(Selector selector) {
    selectors = LessUtils.initList(selectors, 2);
    selectors.add(selector);
    evaluate |= selector.needsEval();
  }
 
  public List<Selector> selectors() {
    return LessUtils.safeList(selectors);
  }
  
  @Override
  public boolean needsEval() {
    return evaluate;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    if (!evaluate) {
      return this;
    }
    Selectors result = new Selectors();
    for (Selector selector : selectors) {
      result.add((Selector)selector.eval(env));
    }
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Selectors) ? LessUtils.safeEquals(selectors, ((Selectors)obj).selectors) : false;
  }

  @Override
  public NodeType type() {
    return NodeType.SELECTORS;
  }
  
  @Override
  public void repr(Buffer buf) {
    int size1 = selectors.size();
    for (int i = 0; i < size1; i++) {
      if (i > 0) {
        buf.append(",\n").indent();
      }
      reprSelector(buf, selectors.get(i));
    }
  }

  public static void reprSelector(Buffer buf, Selector selector) {
    List<Element> elements = selector.elements();
    int size = elements.size();
    for (int i = 0; i < size; i++) {
      reprElement(buf, elements.get(i), i == 0);
    }
  }
  
  private static void reprElement(Buffer buf, Element element, boolean isFirst) {
    Combinator combinator = element.combinator();
    if (combinator != null) {
      boolean isDescendant = combinator == Combinator.DESC;
      char ch = combinator.getChar();
      if (isFirst) {
        if (!isDescendant) {
          buf.append(ch);
        }
      } else {
        if (!isDescendant) {
          buf.append(' ');
        }
        if (!isDescendant || !CharClass.whitespace(buf.prevChar())) {
          buf.append(ch);
        }
      }
      if (!element.isWildcard() && !CharClass.whitespace(buf.prevChar())) {
        buf.append(' ');
      }
    }
    element.repr(buf);
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, selectors);
    buf.decrIndent();
  }
  
}
