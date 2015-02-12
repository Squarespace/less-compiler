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

package com.squarespace.less.model;

import java.util.HashMap;
import java.util.Map;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessUtils;


/**
 * Represents an array of rules.
 *
 * During evaluation some instructions such as {@link Import} or {@link MixinCall},
 * when produce one or more new rules.  These rules can be spliced into the
 * block, replacing the node which produced them.
 *
 * For this reason, blocks manage their arrays of objects directly.
 *
 * Each block contains a set of flags which are used to avoid unnecessary
 * scans of its rules during evaluation.
 */
public class Block extends BaseNode {

  /**
   * Variable cache needs to be rebuilt.
   */
  private static final byte FLAG_REBUILD_VARS = 0x01;

  /**
   * Block contains import statements which need to be evaluated.
   */
  private static final byte FLAG_HAS_IMPORTS = 0x02;

  /**
   * Block contains mixin calls which need to be evaluated.
   */
  private static final byte FLAG_HAS_MIXIN_CALLS = 0x04;

  /**
   * Block contains one or more properties with merge modes.
   */
  private static final byte FLAG_HAS_MERGE_MODES = 0x08;

  /**
   * Initial capacity of the blocks array.
   */
  private static final int INITIAL_CAPACITY = 8;

  /**
   * List of nodes contained within this block.
   */
  protected final FlexList<Node> rules;

  /**
   * Charset directive, if any, associated with this block.
   */
  protected Directive charset;

  /**
   * Variable cache. Since variables can be dynamically added to a block
   * during execution, periodically this cache needs to be rebuilt so that
   * these new variables are found during evaluation.
   */
  protected Map<String, Definition> variables;

  /**
   * Initial flags controlling this block. On creation we need to build the
   * variable cache.
   */
  protected byte flags = FLAG_REBUILD_VARS;

  /**
   * Constructs a block with the default initial capcity.
   */
  public Block() {
    this(INITIAL_CAPACITY);
  }

  /**
   * Constructs a block with the given initial capacity.
   */
  public Block(int initialCapacity) {
    rules = new FlexList<>(initialCapacity);
  }

  /**
   * Private constructor, used by the {@link Block#copy()} method.
   */
  private Block(FlexList<Node> rules, byte flags) {
    this.rules = rules;
    this.flags = flags;
  }

  /**
   * Sets the charset {@link Directive} for this block.
   */
  public void charset(Directive charset) {
    this.charset = charset;
  }

  /**
   * Returns the charset {@link Directive} for this block, if any.
   */
  public Directive charset() {
    return charset;
  }

  /**
   * Inserts a {@link Node} at the head of the block.
   */
  public void prependNode(Node node) {
    setFlags(node);
    rules.splice(0, 0, new Node[] { node });
  }

  /**
   * Appends a {@link Node} to the tail of the block.
   */
  public void appendNode(Node node) {
    setFlags(node);
    rules.append(node);
  }

  /**
   * Appends all {@link Node}s from the argument {@code Block} to
   * the tail of this instance.
   */
  public void appendBlock(Block block) {
    flags |= block.flags;
    rules.append(block.rules);
  }

  /**
   * Returns the internal {@link FlexList} holding the block's rules.
   */
  public FlexList<Node> rules() {
    return rules;
  }

  /**
   * Indicate whether this block contains {@link Import} node.
   */
  public boolean hasImports() {
    return (flags & FLAG_HAS_IMPORTS) != 0;
  }

  /**
   * Indicate whether this block contains a {@link MixinCall} node.
   */
  public boolean hasMixinCalls() {
    return (flags & FLAG_HAS_MIXIN_CALLS) != 0;
  }

  /**
   * Indicate whether this block contains at least one rule whose
   * property has a merge mode set.
   *
   * This indicates the block must defer output during rendering, to
   * combine the values of rules whose properties have the same name.
   */
  public boolean hasPropertyMergeModes() {
    return (flags & FLAG_HAS_MERGE_MODES) != 0;
  }

  /**
   * Mark that the variable cache needs to be rebuilt.
   */
  public void resetVariableCache() {
    flags |= FLAG_REBUILD_VARS;
  }

  /**
   * Resolve a {@link Definition} with the given {@code name} against
   * the rules in this block.
   */
  public Definition resolveDefinition(String name) {
    if ((flags & FLAG_REBUILD_VARS) != 0) {
      buildVariables();
    }
    return variables.get(name);
  }

  /**
   * Builds the variable cache by locating all {@link Definition} nodes
   * within the block, and mapping them by name.
   */
  private void buildVariables() {
    if (variables == null) {
      variables = new HashMap<>();
    } else {
      variables.clear();
    }
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (!node.type().equals(NodeType.DEFINITION)) {
        continue;
      }
      Definition def = (Definition)node;
      variables.put(def.name(), def);
    }
    flags &= ~FLAG_REBUILD_VARS;
  }

  /**
   * Create a shallow copy of this block.
   */
  public Block copy() {
    return new Block(rules.copy(), flags);
  }

  /**
   * Sets this instance's flags by OR-ing with the arguments flags.
   */
  public void orFlags(Block block) {
    flags |= block.flags;
  }

  /**
   * Debug method - collects the definitions inside this block.
   */
  public String dumpDefs() {
    Buffer buf = new Buffer(4);
    dumpDefs(buf);
    return buf.toString();
  }

  /**
   * Debug method - collects the definitions inside this block.
   */
  public boolean dumpDefs(Buffer buf) {
    boolean output = false;
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (node.type().equals(NodeType.DEFINITION)) {
        String repr = node.toString().replaceAll("\\s+", " ");
        buf.indent().append(repr).append('\n');
        output = true;
      }
    }
    return output;
  }

  /**
   * See {@link Node#type()}
   */
  @Override
  public NodeType type() {
    return NodeType.BLOCK;
  }

  /**
   * See {@link Node#repr()}
   */
  @Override
  public void repr(Buffer buf) {
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node rule = rules.get(i);
      if (rule == null) {
        continue;
      }
      if (!buf.compress()) {
        buf.indent();
      }
      rule.repr(buf);
      if (!(rule instanceof BlockNode) && !(rule instanceof Comment)) {
        if (!buf.compress() || i + 1 < size) {
          buf.append(';');
        }
        if (!buf.compress()) {
          buf.append('\n');
        }
      }
    }
  }

  /**
   * See {@link Node#modelRepr(Buffer)}
   */
  @Override
  public void modelRepr(Buffer buf) {
    if (rules != null) {
      typeRepr(buf);
      buf.append('\n');
      buf.incrIndent();
      ReprUtils.modelRepr(buf, "\n", true, rules);
      buf.decrIndent();
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Block) {
      return LessUtils.safeEquals(rules, ((Block)obj).rules);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return rules.toString();
  }

  /**
   * Sets this block's flags based on type of this node.
   */
  private void setFlags(Node node) {
    if (node instanceof Import) {
      flags |= FLAG_HAS_IMPORTS;
    } else if (node instanceof MixinCall) {
      flags |= FLAG_HAS_MIXIN_CALLS;
    } else if (node instanceof Property) {
      Property property = (Property)node;
      if (property.mergeMode() != PropertyMergeMode.NONE) {
        flags |= FLAG_HAS_MERGE_MODES;
      }
    } else if (node instanceof CompositeProperty) {
      CompositeProperty property = (CompositeProperty)node;
      if (property.mergeMode() != PropertyMergeMode.NONE) {
        flags |= FLAG_HAS_MERGE_MODES;
      }
    }
  }

}
