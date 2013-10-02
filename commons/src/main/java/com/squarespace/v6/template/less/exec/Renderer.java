package com.squarespace.v6.template.less.exec;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.core.Chars;
import com.squarespace.v6.template.less.model.Alpha;
import com.squarespace.v6.template.less.model.Assignment;
import com.squarespace.v6.template.less.model.AttributeElement;
import com.squarespace.v6.template.less.model.BaseColor;
import com.squarespace.v6.template.less.model.Combinator;
import com.squarespace.v6.template.less.model.Comment;
import com.squarespace.v6.template.less.model.Directive;
import com.squarespace.v6.template.less.model.Element;
import com.squarespace.v6.template.less.model.Expression;
import com.squarespace.v6.template.less.model.ExpressionList;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.FunctionCall;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Paren;
import com.squarespace.v6.template.less.model.Quoted;
import com.squarespace.v6.template.less.model.Rule;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.Selectors;
import com.squarespace.v6.template.less.model.Shorthand;
import com.squarespace.v6.template.less.model.TextElement;
import com.squarespace.v6.template.less.model.Url;
import com.squarespace.v6.template.less.model.ValueElement;


/**
 * Renders Node instances into strings using a reusable buffer.
 */
public class Renderer {

  private List<Buffer> bufferList = new ArrayList<>();

  private int index;
  
  public Renderer() {
  }
  
  public String render(Context ctx, Node node) throws LessException {
    Buffer buf = acquireBuffer(ctx);
    render(buf, node);
    returnBuffer();
    return buf.toString();
  }

  /**
   * Acquire a reusable buffer to render nodes.  This kills the 
   * @param env
   * @return
   */
  private Buffer acquireBuffer(Context ctx) {
    // Need to grow the list.
    Buffer buf = null;
    if (index == bufferList.size()) {
      buf = ctx.newBuffer();
      bufferList.add(buf);

    } else {
      // Reuse a pre-allocated buffer in the list
      buf = bufferList.get(index);
      buf.reset();
    }

    index++;
    return buf;
  }
  
  public void returnBuffer() {
    index--;
  }
  
  public void render(Buffer buf, Node node) throws LessException {
    if (node == null) {
      return;
    }

    // Nodes which are composed of other nodes are output here.
    switch (node.type()) {
        
      case ALPHA:
        _render(buf, (Alpha)node);
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
        _render(buf, (Assignment)node);
        break;
        
      case COLOR:
        ((BaseColor)node).toRGB().repr(buf);
        break;
        
      case COMMENT:
        _render(buf, (Comment)node);
        break;
        
      case DIRECTIVE:
        _render(buf, (Directive)node); 
        break;
        
      case EXPRESSION:
        _render(buf, (Expression)node); 
        break;
      
      case EXPRESSION_LIST:
        _render(buf, (ExpressionList)node); 
        break;
        
      case FEATURES:  
        _render(buf, (Features)node); 
        break;
      
      case FUNCTION_CALL:
        _render(buf, (FunctionCall)node); 
        break;
        
      case PAREN:
        _render(buf, (Paren)node); 
        break;
        
      case QUOTED:
        _render(buf, (Quoted)node); 
        break;
        
      case RULE:
        _render(buf, (Rule)node); 
        break;
        
      case SELECTORS:
        for (Selector selector : ((Selectors)node).selectors()) {
          _render(buf, selector);
        }
        break;
        
      case SELECTOR:
        _render(buf, (Selector)node); 
        break;
        
      case SHORTHAND:
        _render(buf, (Shorthand)node); 
        break;
        
      case URL:
        _render(buf, (Url)node); 
        break;
        
      default:
        throw new RuntimeException("Don't know how to render a node of type " + node.type());
    }
  }
  
  private void _render(Buffer buf, Alpha alpha) throws LessException {
    buf.append("alpha(opacity=");
    render(buf, alpha.value());
    buf.append(')');
  }
  
  private void _render(Buffer buf, Assignment assign) throws LessException {
    buf.append(assign.name()).append('=');
    render(buf, assign.value());
  }
  
  private void _render(Buffer buf, Comment comment) throws LessException {
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
  
  private void _render(Buffer buf, Directive directive) throws LessException {
    buf.append(directive.name());
    Node value = directive.value();
    if (value != null) {
      buf.append(' ');
      render(buf, value);
    }
  }
  
  private void _render(Buffer buf, Expression expn) throws LessException {
    List<Node> values = expn.values();
    int size = values.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(' ');
      }
      render(buf, values.get(i));
    }
  }

  private void _render(Buffer buf, ExpressionList list) throws LessException {
    List<Node> expns = list.expressions();
    int size = expns.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      render(buf, expns.get(i));
    }
  }
  
  private void _render(Buffer buf, Features features) throws LessException {
    List<Node> nodes = features.features();
    int size = nodes.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.listSep();
      }
      render(buf, nodes.get(i));
    }
  }
  
  private void _render(Buffer buf, FunctionCall call) throws LessException {
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
  private void _render(Buffer buf, Paren paren) throws LessException {
    buf.append('(');
    render(buf, paren.getNode());
    buf.append(')');
  }
  
  /**
   * Render a QUOTED node.
   */
  private void _render(Buffer buf, Quoted quoted) throws LessException {
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
  public void _render(Buffer buf, Rule rule) throws LessException {
    render(buf, rule.property());
    buf.ruleSep();
    render(buf, rule.value());
    if (rule.important()) {
      buf.append(" !important");
    }
  }
  
  /** Render a SHORTHAND node. */
  public void _render(Buffer buf, Shorthand shorthand) throws LessException {
    render(buf, shorthand.left());
    buf.append('/');
    render(buf, shorthand.right());
  }
  
  /** Render a SELECTOR node. */
  private void _render(Buffer buf, Selector selector) throws LessException {
    List<Element> elements = selector.elements();
    int size = elements.size();
    for (int i = 0; i < size; i++) {
      _render(buf, elements.get(i), i == 0);
    }
  }

  /**
   * Handles rendering all of the ELEMENT nodes for a SELECTOR.  The complexity in the first half
   * of this method is required to properly emit whitespace before and after combinators, depending
   * on if we're in compress mode or not.
   */
  private void _render(Buffer buf, Element element, boolean isFirst) throws LessException {
    Combinator combinator = element.combinator();
    if (combinator != null) {
      boolean isDescendant = combinator == Combinator.DESC;
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
  private void _render(Buffer buf, Url url) throws LessException {
    buf.append("url(");
    render(buf, url.value());
    buf.append(Chars.RIGHT_PARENTHESIS);
  }
  
}
