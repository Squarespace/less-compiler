package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.exec.ExecEnv;


public class Selector extends BaseNode {

  private static final int DEFAULT_CAPACITY = 4;
  
  private List<Element> elements;
  
  private boolean hasWildcard;
  
  private boolean evaluate;
  
  public boolean hasWildcard() {
    return hasWildcard;
  }
  
  public void add(Element element) {
    elements = LessUtils.initList(elements, DEFAULT_CAPACITY);
    elements.add(element);
    evaluate |= element.needsEval();
    if (element.isWildcard()) {
      hasWildcard = true;
    }
  }

  public List<Element> elements() {
    return LessUtils.safeList(elements);
  }
  
  public int size() {
    return (elements == null) ? 0 : elements.size();
  }
  
  public boolean isEmpty() {
    return elements == null || (elements.size() == 0);
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
    Selector result = new Selector();
    for (Element elem : elements) {
      result.add((Element)elem.eval(env));
    }
    return result;
  }
  
  @Override
  public boolean equals(Object obj) {
    return (obj instanceof Selector) ? safeEquals(elements, ((Selector)obj).elements) : false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.SELECTOR;
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append('\n');
    buf.incrIndent();
    ReprUtils.modelRepr(buf, "\n", true, elements);
    buf.decrIndent();
  }
  
}
