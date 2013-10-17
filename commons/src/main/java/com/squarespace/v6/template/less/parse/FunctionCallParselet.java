package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.core.SyntaxErrorMaker.expected;
import static com.squarespace.v6.template.less.parse.Parselets.ALPHA;
import static com.squarespace.v6.template.less.parse.Parselets.ASSIGNMENT;
import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION;
import static com.squarespace.v6.template.less.parse.Parselets.QUOTED;
import static com.squarespace.v6.template.less.parse.Parselets.VARIABLE;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Anonymous;
import com.squarespace.v6.template.less.model.ExpressionList;
import com.squarespace.v6.template.less.model.FunctionCall;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Url;


/**
 * Parse all forms which look like function calls.
 */
public class FunctionCallParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark position = stm.mark();
    if (!CharClass.callStart(stm.peek()) || !stm.matchCallName()) {
      return null;
    }

    String name = stm.token();
    String nameLC = name.toLowerCase();
    if (nameLC.equals("url")) {
      return parseUrl(stm);
    
    } else if (nameLC.equals("alpha")) {
      // Special handling for IE's alpha function.
      Node result = stm.parse(ALPHA);
      if (result != null) {
        return result;
      }
      // Fall through, assuming the built-in alpha function.

    }
  
    // Use the lowercase version of the name to match less.js. CSS is case-insensitive
    // within the ASCII range.
    FunctionCall call = new FunctionCall(nameLC);
    ExpressionList args = parseArgs(stm);
    if (nameLC.equals("calc")) {
      call.add(new Anonymous(args.repr()));
    } else {
      for (Node arg : args.expressions()) {
        call.add(arg);
      }
    }
      
    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      stm.restore(position);
      return null;
    }
    return call;
  }
  

  private ExpressionList parseArgs(LessStream stm) throws LessException {
    ExpressionList args = new ExpressionList();
    while (true) {
      Node value = stm.parse(ASSIGNMENT, EXPRESSION);
      if (value != null) {
        args.add(value);
      }
      
      stm.skipWs();
      if (!stm.seekIf(Chars.COMMA)) {
        break;
      }
    }
    return args;
  }

  public static Node parseUrl(LessStream stm) throws LessException {
    stm.skipWs();
    Node value = stm.parse(QUOTED, VARIABLE);
    if (value == null && stm.matchUrlEndBare()) {
      value = new Anonymous(stm.token());
    }
    stm.skipWs();
    if (!stm.seekIf(Chars.RIGHT_PARENTHESIS)) {
      throw stm.parseError(new LessException(expected("right parenthesis ')' to end url")));
    }
    return new Url(value);
  }
  
}
