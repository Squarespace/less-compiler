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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
public class Block implements Node {

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
   * Ordered list of mixin definitions contained in this block that share a
   * common prefix.
   */
  protected Map<String, List<Node>> mixins;

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
  private Block(FlexList<Node> rules, byte flags, Map<String, List<Node>> mixins) {
    this.rules = rules;
    this.flags = flags;
    this.mixins = copyMixins(mixins);
  }

  /**
   * Deep copy the mixin index.
   */
  private Map<String, List<Node>> copyMixins(Map<String, List<Node>> mixins) {
    if (mixins == null) {
      return null;
    }
    Map<String, List<Node>> result = new HashMap<>();
    for (Map.Entry<String, List<Node>> entry : mixins.entrySet()) {
      result.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }
    return result;
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
    FlexList<Node> rules = block.rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      this.appendNode(rules.get(i));
    }
  }

  /**
   * Returns the internal {@link FlexList} holding the block's rules.
   */
  public FlexList<Node> rules() {
    return rules;
  }

  public void splice(int start, int num, FlexList<Node> other) {
    this.rules.splice(start, num, other);
    int size = other.size();
    for (int i = 0; i < size; i++) {
      setFlags(other.get(i));
    }
  }

  public Map<String, List<Node>> mixins() {
    return this.mixins;
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
    return new Block(rules.copy(), flags, mixins);
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
//  public String dumpDefs() {
//    Buffer buf = new Buffer(4);
//    dumpDefs(buf);
//    return buf.toString();
//  }

  /**
   * Debug method - collects the definitions inside this block.
   */
//  public boolean dumpDefs(Buffer buf) {
//    boolean output = false;
//    int size = rules.size();
//    for (int i = 0; i < size; i++) {
//      Node node = rules.get(i);
//      if (node.type().equals(NodeType.DEFINITION)) {
//        String repr = node.toString().replaceAll("\\s+", " ");
//        buf.indent().append(repr).append('\n');
//        output = true;
//      }
//    }
//    return output;
//  }

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
      ReprUtils.modelRepr(buf, "\n", true, rules);
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
    return ModelUtils.notHashable();
  }

  @Override
  public String toString() {
    return rules.toString();
  }

  /**
   * Updates this block's flags and mixin index.
   */
  private void setFlags(Node node) {
    if (node instanceof Import) {
      flags |= FLAG_HAS_IMPORTS;
    } else if (node instanceof MixinCall) {
      flags |= FLAG_HAS_MIXIN_CALLS;
    } else if (node instanceof Mixin) {
      this.indexMixin(((Mixin)node).name, node);
    } else if (node instanceof Ruleset) {
      Ruleset ruleset = (Ruleset)node;
      Selectors selectors = ruleset.selectors();
      if (selectors.hasMixinPath()) {
        List<Selector> _selectors = selectors.selectors();
        int size = _selectors.size();
        for (int i = 0; i < size; i++) {
          Selector selector = _selectors.get(i);
          List<String> path = selector.mixinPath();
          if (path != null) {
            this.indexMixin(path.get(0), node);
          }
        }
      }
    }
  }

  private void indexMixin(String prefix, Node node) {
    // Optimization for fast prefix matching of mixin paths.
    // Index the ruleset and mixin paths by the first segment
    // of their path. This lets is prune the search tree drastically
    // by (a) ignoring branches with no valid prefix and (b) iterating
    // over just the mixins when a prefix is valid.

    if (this.mixins == null) {
      this.mixins = new HashMap<>();
    }

    List<Node> nodes = this.mixins.get(prefix);
    if (nodes == null) {
      nodes = new ArrayList<>();
      this.mixins.put(prefix, nodes);
    }
    nodes.add(node);
  }
}
