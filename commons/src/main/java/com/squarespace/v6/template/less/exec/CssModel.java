package com.squarespace.v6.template.less.exec;

import static com.squarespace.v6.template.less.model.NodeType.BLOCK_DIRECTIVE;
import static com.squarespace.v6.template.less.model.NodeType.MEDIA;
import static com.squarespace.v6.template.less.model.NodeType.RULESET;
import static com.squarespace.v6.template.less.model.NodeType.STYLESHEET;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.LessUtils;
import com.squarespace.v6.template.less.model.NodeType;

/**
 * Extremely simplistic model for CSS structure. Allows the LESS renderer
 * to defer final output in order to suppress empty blocks, and eliminate
 * duplicate rules.  It also ensures that each nested block is emitted
 * in the output model at the correct scope.
 */
public class CssModel {

  private static final EnumSet<NodeType> STYLESHEET_ACCEPT = EnumSet.of(
      STYLESHEET, RULESET, MEDIA, BLOCK_DIRECTIVE
      );
  
  private static final EnumSet<NodeType> MEDIA_ACCEPT = EnumSet.of(
      BLOCK_DIRECTIVE, RULESET
      );
  
  private static final EnumSet<NodeType> BLOCK_DIRECTIVE_ACCEPT = EnumSet.of(
      BLOCK_DIRECTIVE, RULESET
      );
  
  private static final EnumSet<NodeType> RULESET_ACCEPT = EnumSet.noneOf(NodeType.class);
  
  private Deque<CssBlock> stack = new ArrayDeque<>();
  
  private Buffer buffer;
  
  private CssBlock current;
  
  public CssModel(Context ctx) {
    buffer = ctx.newBuffer();
    current = new CssBlock(STYLESHEET);
  }
  
  public void reset() {
    current = new CssBlock(STYLESHEET);
  }
  
  public String render() {
    if (current.type() != STYLESHEET) {
      throw new RuntimeException("Serious error: stack was not fully popped.");
    }
    buffer.reset();
    current.render(buffer);
    return buffer.toString();
  }

  public CssModel value(String value) {
    current.add(new CssValue(value));
    return this;
  }
  
  public CssModel comment(String value) {
    current.add(new CssComment(value));
    return this;
  }

  public CssModel header(String ... strings) {
    for (String raw : strings) {
      current.add(raw);
    }
    return this;
  }
  
  public CssModel push(NodeType type) {
    stack.push(current);
    CssBlock child = new CssBlock(type);
    defer(child);
    current = child;
    return this;
  }
  
  public CssModel pop() {
    CssBlock parent = current.parent();
    parent.populated |= current.populated;
    current = stack.pop();
    return this;
  }
  
  /**
   * Push this block up the stack until it finds its proper home.
   */
  private void defer(CssBlock block) {
    if (current.accept(block)) {
      return;
    }
    Iterator<CssBlock> iter = stack.iterator();
    while (iter.hasNext()) {
      CssBlock candidate = iter.next();
      if (candidate.accept(block)) {
        return;
      }
    }
    throw new RuntimeException("Serious error: no block accepted " + block.type());
  }
  
  static class CssBlock extends CssNode {

    private List<String> headers = new ArrayList<>();

    private Set<CssNode> nodes = new LinkedHashSet<>();
    
    private CssBlock parent;
    
    private NodeType type;
    
    private EnumSet<NodeType> acceptFilter;
    
    private boolean populated = false;

    public CssBlock(NodeType type) {
      this.type = type;
      switch (type) {

        case BLOCK_DIRECTIVE:
          acceptFilter = BLOCK_DIRECTIVE_ACCEPT;
          break;
          
        case MEDIA:
          acceptFilter = MEDIA_ACCEPT;
          break;
          
        case RULESET:
          acceptFilter = RULESET_ACCEPT;
          break;
          
        case STYLESHEET:
          acceptFilter = STYLESHEET_ACCEPT;
          break;
          
        default:
          throw new RuntimeException("Serious error: css model block cannot be " + type);
      }
    }

    public boolean accept(CssBlock block) {
      if (acceptFilter.contains(block.type())) {
        add(block);
        block.setParent(this);
        return true;
      }
      return false;
    }
    
    public CssBlock parent() {
      return parent;
    }
    
    public void setParent(CssBlock parent) {
      this.parent = parent;
    }
    
    public NodeType type() {
      return type;
    }
    
    public boolean populated() {
      return populated;
    }
    
    public void add(String header) {
      headers.add(header);
    }
    
    public void add(CssNode node) {
      // Ensure that the last unique rule (key + value) wins.
      if (nodes.contains(node)) {
        nodes.remove(node);
      }
      nodes.add(node);
      populated |= node.populated();
    }

    @Override
    public boolean isValue() {
      return false;
    }

    @Override
    public void render(Buffer buf) {
      if (!populated) {
        return;
      }

      if (!headers.isEmpty()) {
        int size = headers.size();
        for (int i = 0; i < size; i++) {
          if (i > 0) {
            buf.selectorSep();
          }
          buf.indent();
          buf.append(headers.get(i));
        }
        buf.blockOpen();
      }
      
      Iterator<CssNode> iter = nodes.iterator();
      while (iter.hasNext()) {
        CssNode node = iter.next();
        node.render(buf);
      }
      if (!headers.isEmpty()) {
        buf.blockClose();
      }
    }
  }
  
  static class CssValue extends CssNode {
    
    private String value;
    
    public CssValue(String value) {
      this.value = value;
    }
    
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof CssValue) ? LessUtils.safeEquals(value, ((CssValue)obj).value) : false;
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }
    
    @Override
    public void render(Buffer buf) {
      buf.indent();
      buf.append(value);
      buf.ruleEnd();
    }
    
  }
  
  static class CssComment extends CssNode { 
    
    private String value;
    
    public CssComment(String value) {
      this.value = value;
    }
    
    @Override
    public boolean equals(Object obj) {
      return (obj instanceof CssComment) ? LessUtils.safeEquals(value, ((CssComment)obj).value) : false;
    }

    @Override
    public int hashCode() {
      return value.hashCode();
    }
    
    @Override
    public void render(Buffer buf) {
      buf.indent().append(value);
    }

  }

  static abstract class CssNode {
    
    public boolean isValue() {
      return true;
    }
    
    public boolean populated() {
      return true;
    }
    
    public abstract void render(Buffer buf);
    
  }
  
}
