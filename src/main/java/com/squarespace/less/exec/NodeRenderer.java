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

package com.squarespace.less.exec;

import java.util.List;

import com.squarespace.less.LessContext;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.CharClass;
import com.squarespace.less.core.Chars;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.Alpha;
import com.squarespace.less.model.Assignment;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.BaseColor;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Feature;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.FunctionCall;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Shorthand;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.Url;
import com.squarespace.less.model.ValueElement;


/**
 * Renders Node instances into strings using reusable buffers.
 */
public class NodeRenderer {

  private NodeRenderer() {
  }

  public static String render(LessContext ctx, Node node) {
    Buffer buf = ctx.acquireBuffer();
    render(buf, node);
    ctx.returnBuffer();
    return buf.toString();
  }

  public static void render(Buffer buf, Node node) {
    if (node == null) {
      return;
    }

    // Nodes which are composed of other nodes are output here.
    switch (node.type()) {

      case ALPHA:
        renderImpl(buf, (Alpha)node);
        break;

      case ANONYMOUS:
      case DIMENSION:
      case FALSE:
      case KEYWORD:
      case PROPERTY:
      case RATIO:
      case TRUE:
      case UNICODE_RANGE:
        node.repr(buf);
        break;

      case ASSIGNMENT:
        renderImpl(buf, (Assignment)node);
        break;

      case COLOR:
        ((BaseColor)node).toRGB().repr(buf);
        break;

      case COMMENT:
        renderImpl(buf, (Comment)node);
        break;

      case DIRECTIVE:
        renderImpl(buf, (Directive)node);
        break;

      case EXPRESSION:
        renderImpl(buf, (Expression)node);
        break;

      case EXPRESSION_LIST:
        renderImpl(buf, (ExpressionList)node);
        break;

      case FEATURE:
        renderImpl(buf, (Feature)node);
        break;

      case FEATURES:
        renderImpl(buf, (Features)node);
        break;

      case FUNCTION_CALL:
        renderImpl(buf, (FunctionCall)node);
        break;

      case PAREN:
        renderImpl(buf, (Paren)node);
        break;

      case QUOTED:
        renderImpl(buf, (Quoted)node);
        break;

      case RULE:
        renderImpl(buf, (Rule)node);
        break;

      case SELECTORS:
        for (Selector selector : ((Selectors)node).selectors()) {
          renderImpl(buf, selector);
        }
        break;

      case SELECTOR:
        renderImpl(buf, (Selector)node);
        break;

      case SHORTHAND:
        renderImpl(buf, (Shorthand)node);
        break;

      case URL:
        renderImpl(buf, (Url)node);
        break;

      default:
        throw new LessInternalException("Don't know how to render a node of type " + node.type());
    }
  }

  private static void renderImpl(Buffer buf, Alpha alpha) {
    buf.append("alpha(opacity=");
    render(buf, alpha.value());
    buf.append(')');
  }

  private static void renderImpl(Buffer buf, Assignment assign) {
    buf.append(assign.name()).append('=');
    render(buf, assign.value());
  }

  private static void renderImpl(Buffer buf, Comment comment) {
    String body = comment.body();
    if (comment.block()) {
      buf.append("/*").append(body).append("*/");
    } else {
      buf.append("//").append(body);
    }
    if (comment.newline()) {
      buf.append(Chars.LINE_FEED);
    }
  }

  private static void renderImpl(Buffer buf, Directive directive) {
    buf.append(directive.name());
    Node value = directive.value();
    if (value != null) {
      buf.append(' ');
      render(buf, value);
    }
  }

  private static void renderImpl(Buffer buf, Expression expn) {
    renderList(buf, expn.values(), " ");
  }

  private static void renderImpl(Buffer buf, ExpressionList list) {
    renderList(buf, list.expressions(), buf.compress() ? "," : ", ");
  }

  private static void renderList(Buffer buf, List<Node> nodes, String sep) {
    int size = nodes.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      render(buf, expns.get(i));
    }
  }

  public static void renderImpl(Buffer buf, Feature feature) {
    render(buf, feature.property());
    buf.ruleSep();
    render(buf, feature.value());
  }

  private static void renderImpl(Buffer buf, Features features) {
    List<Node> nodes = features.features();
    int size = nodes.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      render(buf, nodes.get(i));
    }
  }

  private static void renderImpl(Buffer buf, FunctionCall call) {
    String name = call.name();
    buf.append(name).append('(');

    List<Node> args = call.args();
    int size = args.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      render(buf, args.get(i));
    }
    buf.append(')');
  }

  /** Render a PAREN node. */
  private static void renderImpl(Buffer buf, Paren paren) {
    buf.append('(');
    render(buf, paren.value());
    buf.append(')');
  }

  /**
   * Render a QUOTED node.
   */
  private static void renderImpl(Buffer buf, Quoted quoted) {
    List<Node> parts = quoted.parts();
    boolean escaped = quoted.escaped();
    char delim = escaped ? Chars.NULL : quoted.delimiter();
    boolean emitDelim = !buf.inEscape();
    if (emitDelim) {
      if (!escaped) {
        buf.append(delim);
      }
      buf.startDelim(delim);
    }

    int size = (parts == null) ? 0 : parts.size();
    for (int i = 0; i < size; i++) {
      render(buf, parts.get(i));
    }

    if (emitDelim) {
      buf.endDelim();
      if (!escaped) {
        buf.append(delim);
      }
    }
  }

  /** Render a RULE node. */
  public static void renderImpl(Buffer buf, Rule rule) {
    render(buf, rule.property());
    buf.ruleSep();
    render(buf, rule.value());
    if (rule.important()) {
      buf.append(" !important");
    }
  }

  /** Render a SHORTHAND node. */
  public static void renderImpl(Buffer buf, Shorthand shorthand) {
    render(buf, shorthand.left());
    buf.append('/');
    render(buf, shorthand.right());
  }

  /** Render a SELECTOR node. */
  private static void renderImpl(Buffer buf, Selector selector) {
    List<Element> elements = selector.elements();
    int size = elements.size();
    for (int i = 0; i < size; i++) {
      renderImpl(buf, elements.get(i), i == 0);
    }
  }

  /**
   * Handles rendering all of the ELEMENT nodes for a SELECTOR.  The complexity in the first half
   * of this method is required to properly emit whitespace before and after combinators, depending
   * on if we're in compress mode or not.
   */
  private static void renderImpl(Buffer buf, Element element, boolean isFirst) {
    Combinator combinator = element.combinator();
    if (combinator != null) {
      boolean isDescendant = combinator == Combinator.DESC;
      if (isDescendant && element.isWildcard()) {
        // This combination just emits useless whitespace. Ignore.
        return;
      }

      char ch = combinator.getChar();

      if (isFirst) {
        if (!isDescendant) {
          buf.append(ch);
        }

      } else {
        if (!buf.compress() && !isDescendant) {
          buf.append(' ');
        }
        if (!isDescendant || !CharClass.whitespace(buf.prevChar())) {
          buf.append(ch);
        }
      }

      if (!buf.compress() && !element.isWildcard() && !CharClass.whitespace(buf.prevChar())) {
        buf.append(' ');
      }
    }

    // Wildcard elements do not produce output
    if (element.isWildcard()) {
      return;
    }

    if (element instanceof TextElement) {
      ((TextElement)element).repr(buf);

    } else if (element instanceof ValueElement) {
      ValueElement varElem = (ValueElement)element;
      render(buf, varElem.value());

    } else if (element instanceof AttributeElement) {
      buf.append('[');
      AttributeElement attrElem = (AttributeElement)element;
      List<Node> parts = attrElem.parts();
      int size = parts.size();
      for (int i = 0; i < size; i++) {
        render(buf, parts.get(i));
      }
      buf.append(']');
    }

  }

  /** Render a URL node. */
  private static void renderImpl(Buffer buf, Url url) {
    buf.append("url(");
    render(buf, url.value());
    buf.append(Chars.RIGHT_PARENTHESIS);
  }

}
