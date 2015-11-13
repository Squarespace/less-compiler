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

import static com.squarespace.less.model.NodeType.BLOCK_DIRECTIVE;
import static com.squarespace.less.model.NodeType.MEDIA;
import static com.squarespace.less.model.NodeType.RULESET;
import static com.squarespace.less.model.NodeType.STYLESHEET;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.squarespace.less.LessContext;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.core.LessUtils;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Stylesheet;

/**
 * Simple model for producing the final CSS output.
 *
 * Allows the LESS renderer to defer final output in order to
 * suppress empty blocks, and eliminate duplicate rules.  It also
 * ensures that each nested block is emitted in the output model
 * at the correct scope.
 */
public class CssModel {

  /**
   * Set of node types that can be children of a {@link Stylesheet}.
   */
  private static final EnumSet<NodeType> STYLESHEET_ACCEPT = EnumSet.of(
      STYLESHEET, RULESET, MEDIA, BLOCK_DIRECTIVE
      );

  /**
   * Set of node types that can be children of a {@link Media} node.
   */
  private static final EnumSet<NodeType> MEDIA_ACCEPT = EnumSet.of(
      BLOCK_DIRECTIVE, RULESET
      );

  /**
   * Set of node types that can be children of a {@link BlockDirective} node.
   */
  private static final EnumSet<NodeType> BLOCK_DIRECTIVE_ACCEPT = EnumSet.of(
      BLOCK_DIRECTIVE, RULESET
      );

  /**
   * Set of node types that can be children of a {@link Ruleset} node.
   */
  private static final EnumSet<NodeType> RULESET_ACCEPT = EnumSet.noneOf(NodeType.class);

  /**
   * Stack of CSS blocks.
   */
  private final Deque<CssBlock> stack = new ArrayDeque<>();

  /**
   * Internal buffer for rendering the CSS output.
   */
  private final Buffer buffer;

  /**
   * Current block being operated on.
   */
  private CssBlock current;

  /**
   * Constructs a CSS model with the given context.
   */
  public CssModel(LessContext ctx) {
    buffer = ctx.newBuffer();
    current = new CssBlock(STYLESHEET);
  }

  /**
   * Renders the CSS model into text form.
   */
  public String render() {
    if (current.type() != STYLESHEET) {
      throw new LessInternalException("Serious error: stack was not fully popped.");
    }
    buffer.reset();
    current.render(buffer);
    return buffer.toString();
  }

  /**
   * Appends a value to the current block.
   */
  public CssModel value(String value) {
    current.add(new CssValue(value));
    return this;
  }

  /**
   * Appends a comment to the current block.
   */
  public CssModel comment(String value) {
    current.add(new CssComment(value));
    return this;
  }

  /**
   * Add raw strings to the header of the current block.
   */
  public CssModel header(String ... strings) {
    for (String raw : strings) {
      current.add(raw);
    }
    return this;
  }

  /**
   * Pushes an empty block onto the stack and associates it with the given node type.
   */
  public CssModel push(NodeType type) {
    stack.push(current);
    CssBlock child = new CssBlock(type);
    defer(child);
    current = child;
    return this;
  }

  /**
   * Pops a block from the top of the stack, setting flags indicating whether
   * anything was appended to the block.  This is used to prune empty blocks.
   */
  public CssModel pop() {
    CssBlock parent = current.parent();
    parent.populated |= current.populated;
    current = stack.pop();
    return this;
  }

  /**
   * Push this block up the stack until it finds its proper parent.
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
    throw new LessInternalException("Serious error: no block accepted " + block.type());
  }

  /**
   * Represents a CSS block that can contain other nodes and blocks.
   */
  static class CssBlock extends CssNode {

    // Adding an element to a LinkedHashSet is about 1/2 as fast as adding it to
    // an ArrayList, but maintains original insertion order while removing duplicates.

    private final Set<String> headers = new LinkedHashSet<>();

    private final Set<CssNode> nodes = new LinkedHashSet<>();

    private final NodeType type;

    private final EnumSet<NodeType> acceptFilter;

    private CssBlock parent;

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
          throw new LessInternalException("Serious error: css model block cannot be " + type);
      }
    }

    /**
     * Determines if this block can accept a block of the given type as a
     * child.
     */
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
        int count = 0;
        for (String header : headers) {
          if (count > 0) {
            buf.selectorSep();
          }
          buf.indent();
          buf.append(header);
          count++;
        }
        buf.blockOpen();
      }

      Iterator<CssNode> iter = nodes.iterator();
      while (iter.hasNext()) {
        CssNode node = iter.next();
        node.render(buf);
        // If we're adding a rule, and we're not compressed or this is not the last
        // rule in the ruleset, append the semicolon.
        if (node instanceof CssValue && (!buf.compress() || iter.hasNext())) {
          buf.ruleEnd();
        }
      }
      if (!headers.isEmpty()) {
        buf.blockClose();
        if (!buf.compress()) {
          buf.append('\n');
        }
      }
    }
  }

  /**
   * Represents a simple value in a CSS model.
   */
  static class CssValue extends CssNode {

    private final String value;

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
    }

  }

  /**
   * Represents a comment in a CSS model.
   */
  static class CssComment extends CssNode {

    private final String value;

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

  /**
   * Abstract node in a CSS model.
   */
  abstract static class CssNode {

    public boolean isValue() {
      return true;
    }

    public boolean populated() {
      return true;
    }

    public abstract void render(Buffer buf);

  }

}
