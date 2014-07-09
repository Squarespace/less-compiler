/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.parse;

import static com.squarespace.less.parse.Parselets.BLOCK;
import static com.squarespace.less.parse.Parselets.DIRECTIVE_IMPORT;
import static com.squarespace.less.parse.Parselets.ENTITY;
import static com.squarespace.less.parse.Parselets.EXPRESSION;
import static com.squarespace.less.parse.Parselets.FEATURES;

import org.apache.commons.lang3.StringUtils;

import com.squarespace.less.LessException;
import com.squarespace.less.core.Chars;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Node;


/**
 * Main parser for all directives of the form '@' NAME.
 */
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

      default:
        break;
    }

    if (hasIdentifier) {
      name += parseIdentifier(stm);
    }

    if (hasBlock) {
      Node block = stm.parse(BLOCK);
      if (block != null) {
        BlockDirective directive = new BlockDirective(name, (Block)block);
        directive.fileName(stm.fileName());
        return directive;
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
    Features features = (Features) stm.parse(FEATURES);
    Node block = stm.parse(BLOCK);
    if (block == null) {
      return null;
    }
    Media media = stm.context().nodeBuilder().buildMedia(features, (Block)block);
    media.fileName(stm.fileName());
    return media;
  }

  private Node parseImport(LessStream stm, String name) throws LessException {
    boolean once = false;
    if (name.endsWith("-once")) {
      once = true;
    }

    Node path = stm.parse(DIRECTIVE_IMPORT);
    if (path == null) {
      return null;
    }

    Features features = (Features) stm.parse(FEATURES);
    stm.skipWs();
    if (stm.seekIf(Chars.SEMICOLON)) {
      Import importNode = new Import(path, features, once);
      importNode.rootPath(stm.rootPath());
      importNode.fileName(stm.fileName());
      return stm.context().importer().importStylesheet(importNode);
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
