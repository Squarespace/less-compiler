package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION_LIST;
import static com.squarespace.v6.template.less.parse.Parselets.FONT;
import static com.squarespace.v6.template.less.parse.Parselets.PROPERTY;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Anonymous;
import com.squarespace.v6.template.less.model.Definition;
import com.squarespace.v6.template.less.model.ExpressionList;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Property;
import com.squarespace.v6.template.less.model.Rule;
import com.squarespace.v6.template.less.model.Variable;


public class RuleParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    if (!CharClass.ruleStart(stm.peek())) {
      return null;
    }
    
    Mark mark = stm.mark();
    Node key = parseKey(stm);
    if (key == null) {
      stm.restore(mark);
      return null;
    }

    String name = null;
    if (key.is(NodeType.PROPERTY)) {
      name = ((Property)key).name();
    } else {
      name = ((Variable)key).name();
    }
    
    Node value = null;
    stm.skipWs();
      
/*    if (name.charAt(0) != Chars.AT_SIGN && stm.matchAnonRuleValue()) {
        value = new Anonymous(stm.token());
        
    } else */ if (name.equals("font")) {
      value = stm.parse(FONT);
      
    } else {
      value = stm.parse(EXPRESSION_LIST);
      if (value != null) {
        // Flatten lists of length 1, simplifying the tree.
        ExpressionList expn = (ExpressionList)value;
        if (expn.size() == 1) {
          value = expn.expressions().get(0);
        }
        
      // Before giving up, try to catch other opaque values.
      } else if (name.charAt(0) != Chars.AT_SIGN && stm.matchAnonRuleValue()) {
        value = new Anonymous(stm.token());
      }
    }
    
    stm.skipWs();
    boolean important = important(stm);
    if (value != null && end(stm)) {
      if (key.is(NodeType.VARIABLE)) {
        // Note that !important is ingored for definitions.
        return new Definition(name, value);
        
      } else {
        return new Rule((Property)key, value, important);
      }
    }
    
    stm.restore(mark);
    return null;
  }
  
  private Node parseKey(LessStream stm) throws LessException {
    Node key = stm.parse(PROPERTY, VARIABLE);
    stm.skipWs();
    if (stm.seekIf(Chars.COLON)) {
      return key;
    }
    return null;
  }
  
  private boolean important(LessStream stm) {
    return (stm.peek() == Chars.EXCLAMATION_MARK && stm.matchImportant());
  }
  
  private boolean end(LessStream stm) {
    stm.skipWs();
    switch (stm.peek()) {
      case Chars.SEMICOLON:
        stm.seek1();
        // Fall through..

      case Chars.EOF:
      case Chars.RIGHT_CURLY_BRACKET:
        return true;
    }
    return false;
  }

}
