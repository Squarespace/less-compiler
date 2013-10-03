package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.SyntaxErrorType.EXPECTED_MISC;
import static com.squarespace.v6.template.less.SyntaxErrorType.MIXED_DELIMITERS;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;
import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Argument;
import com.squarespace.v6.template.less.model.ExpressionList;
import com.squarespace.v6.template.less.model.MixinCallArgs;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Variable;


public class MixinCallArgsParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    stm.skipWs();
    if (!stm.seekIf(Chars.LEFT_PARENTHESIS)) {
      return null;
    }
    
    boolean delimSemicolon = false;
    boolean containsNamed = false;
    MixinCallArgs argsComma = new MixinCallArgs(Chars.COMMA);
    MixinCallArgs argsSemicolon = new MixinCallArgs(Chars.SEMICOLON);
    ExpressionList expressions = new ExpressionList();
    String name = null;
    Node node = null;
    
    while ((node = stm.parse(EXPRESSION)) != null) {
      String nameLoop = null;
      Node value = node;

      // If the last node parsed is a variable reference, check if it is a named argument.
      // If not, treat the variable as a pass-by-reference value.
      
      // Examples:   (@x, @y)      - both @x and @y are pass-by-reference
      //             (@x: 1, @y)   - @x is pass-by-value named argument, @y is pass-by-reference
      //
      if (node.is(NodeType.VARIABLE)) {
        Variable var = (Variable)node;
        Node temp = parseVarArg(stm, expressions.size());
        if (temp != null) {
          if (expressions.size() > 0) {
            if (delimSemicolon) {
              throw new LessException(error(MIXED_DELIMITERS));
            }
            containsNamed = true;
          }
          value = temp;
          nameLoop = name = var.name();
        }
      }

      expressions.add(value);
      argsComma.add(new Argument(nameLoop, value));
      
      stm.skipWs();
      if (stm.seekIf(Chars.COMMA)) {
        continue;
      }
      
      // Detect whether the arguments are semicolon delimited.
      if (stm.seekIf(Chars.SEMICOLON)) {
        delimSemicolon = true;

      } else if (!delimSemicolon) {
        continue;
      }
      
      // Handle semicolon-delimited arguments.
      if (containsNamed) {
        throw new LessException(error(MIXED_DELIMITERS));
      }
      if (expressions.size() > 1) {
        value = expressions;
      }

      argsSemicolon.add(new Argument(name, value));
      name = null;
      expressions = new ExpressionList();
      containsNamed = false;
    }
    
    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw new LessException(error(EXPECTED_MISC).arg0(')').arg1(stm.remainder()));
    }

    return delimSemicolon ? argsSemicolon : argsComma;
  }

  private Node parseVarArg(LessStream stm, int expCount) throws LessException {
    if (!stm.seekIf(Chars.COLON)) {
      return null;
    }
    Node value = stm.parse(EXPRESSION);
    if (value == null) {
      throw new LessException(error(EXPECTED_MISC).arg0("expression").arg1(stm.remainder()));
    }
    return value;
  }
  
}
