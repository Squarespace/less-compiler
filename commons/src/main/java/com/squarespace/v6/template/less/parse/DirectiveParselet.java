package com.squarespace.v6.template.less.parse;

import static com.squarespace.v6.template.less.parse.Parselets.ENTITY;
import static com.squarespace.v6.template.less.parse.Parselets.EXPRESSION;
import static com.squarespace.v6.template.less.parse.Parselets.QUOTED;
import static com.squarespace.v6.template.less.parse.Parselets.URL;

import org.apache.commons.lang3.StringUtils;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.BlockDirective;
import com.squarespace.v6.template.less.model.Directive;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Import;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.Node;


public class DirectiveParselet implements Parselet {

  @Override
  public Node parse(LessStream stm) throws LessException {
    Mark mark = stm.mark();
    stm.skipWs();
    if (stm.peek() != Chars.AT_SIGN || !stm.matchDirective()) {
      return null;
    }
    String name = stm.token();
    String nvName = name;
    if (name.charAt(1) == Chars.MINUS_SIGN) {
      int index = name.indexOf(Chars.MINUS_SIGN, 2);
      if (index > 0) {
        nvName = "@" + name.substring(index + 1);
      }
    }

    boolean hasBlock = false;
    boolean hasExpression = false;
    boolean hasIdentifier = false;
    switch (nvName) {
      
      case "@import":
      case "@import-once":
        Node result = parseImport(stm, nvName);
        if (result == null) {
          stm.restore(mark);
        }
        return result;
      
      case "@media":
        return parseMedia(stm);

      case "@font-face":
      case "@viewport":
      case "@top-left":
      case "@top-left-corner":
      case "@top-center":
      case "@top-right":
      case "@top-right-corner":
      case "@bottom-left":
      case "@bottom-left-corner":
      case "@bottom-center":
      case "@bottom-right":
      case "@bottom-right-corner":
      case "@left-top":
      case "@left-middle":
      case "@left-bottom":
      case "@right-top":
      case "@right-middle":
      case "@right-bottom":
          hasBlock = true;
          break;
          
      case "@page":
      case "@document":
      case "@supports":
      case "@keyframes":
          hasBlock = true;
          hasIdentifier = true;
          break;
          
      case "@namespace":
          hasExpression = true;
          break;
    }
    
    if (hasIdentifier) {
      name += parseIdentifier(stm);
    }
    
    if (hasBlock) {
      Node block = stm.parse(Parselets.BLOCK);
      if (block != null) {
        return new BlockDirective(name, (Block)block);
      }
      
    } else {
      Node value = parseRest(stm, hasExpression);
      if (value != null) {
        return new Directive(name, value);
      }
    }

    stm.restore(mark);
    return null;
  }

  private Node parseMedia(LessStream stm) throws LessException {
    Features features = (Features) stm.parse(Parselets.FEATURES);
    Node block = stm.parse(Parselets.BLOCK);
    if (block == null) {
      return null;
    }
    Media media = new Media(features);
    media.setBlock((Block)block);
    return media;
  }
  
  private Node parseImport(LessStream stm, String name) throws LessException {
    boolean once = false;
    if (name.endsWith("-once")) {
      once = true;
    }
    
    Node path = stm.parse(QUOTED, URL);
    if (path == null) {
      return null;
    }

    Features features = (Features) stm.parse(Parselets.FEATURES);
    stm.skipWs();
    if (stm.seekIf(Chars.SEMICOLON)) {
      Import result = new Import(path, features, once);
      result.setRootPath(stm.rootPath());
      return result;
    }
    return null;
  }
  
  private String parseIdentifier(LessStream stm) throws LessException {
    StringBuilder buf = new StringBuilder();
    stm.skipWs();
    char ch = stm.peek();
    while (ch != Chars.NULL && ch != Chars.LEFT_CURLY_BRACKET && ch != Chars.LEFT_SQUARE_BRACKET) {
      buf.append(ch);
      stm.seek1();
      ch = stm.peek();
    }
    return " " + StringUtils.strip(buf.toString());
  }
  
  private Node parseRest(LessStream stm, boolean hasExpression) throws LessException {
    Node value = (hasExpression) ? stm.parse(EXPRESSION) : stm.parse(ENTITY);
    stm.skipWs();
    if (stm.seekIf(Chars.SEMICOLON)) {
      return value;
    }
    return null;
  }
  
}
