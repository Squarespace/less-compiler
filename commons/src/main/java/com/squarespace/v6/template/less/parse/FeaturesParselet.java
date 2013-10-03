package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.FEATURE;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Node;


public class FeaturesParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Node node = parseOne(stm);
    if (node == null) {
      return null;
    }

    Features features = new Features();
    while (node != null) {
      features.add(node);
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
      node = parseOne(stm);
    }
    return features;
  }

  private Node parseOne(LessStream stm) throws LessException {
    Node node = stm.parse(FEATURE);
    if (node != null) {
      return node;
    }
    return stm.parse(VARIABLE);
  }

}
