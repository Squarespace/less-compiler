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
 * Represents an array of rules which can be evaluated. A given rule,
 * such an IMPORT or MIXIN_CALL can be expanded into multiple rules which
 * will be spliced into the array.  For this reason, blocks manage their
 * arrays of objects directly.
 *
 * Each block contains a set of flags which are used to avoid unnecessary
 * scans of the block's rules during evaluation.
 */
public class Block extends BaseNode {

  private static final byte FLAG_REBUILD_VARS = 0x01;

  private static final byte FLAG_HAS_IMPORTS = 0x02;

  private static final byte FLAG_HAS_MIXIN_CALLS = 0x04;

  private static final int INITIAL_CAPACITY = 8;

  protected final FlexList<Node> rules;

  protected Directive charset;

  protected Map<String, Definition> variables;

  protected byte flags = FLAG_REBUILD_VARS;

  public Block() {
    this(INITIAL_CAPACITY);
  }

  private Block(FlexList<Node> rules, byte flags) {
    this.rules = rules;
    this.flags = flags;
  }

  public Block(int initialCapacity) {
    rules = new FlexList<>(initialCapacity);
  }

  public void charset(Directive charset) {
    this.charset = charset;
  }

  public Directive charset() {
    return charset;
  }

  public void prependNode(Node node) {
    setFlags(node);
    rules.splice(0, 0, new Node[] { node });
  }

  public void appendNode(Node node) {
    setFlags(node);
    rules.append(node);
  }

  public void appendBlock(Block block) {
    flags |= block.flags;
    rules.append(block.rules);
  }

  public FlexList<Node> rules() {
    return rules;
  }

  public boolean hasImports() {
    return (flags & FLAG_HAS_IMPORTS) != 0;
  }

  public boolean hasMixinCalls() {
    return (flags & FLAG_HAS_MIXIN_CALLS) != 0;
  }

  public void resetCache() {
    flags |= FLAG_REBUILD_VARS;
  }

  public Definition resolveDefinition(String name) {
    if ((flags & FLAG_REBUILD_VARS) != 0) {
      buildVariables();
    }
    return variables.get(name);
  }

  private void buildVariables() {
    if (variables == null) {
      variables = new HashMap<>();
    } else {
      variables.clear();
    }
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (!node.is(NodeType.DEFINITION)) {
        continue;
      }
      Definition def = (Definition)node;
      variables.put(def.name(), def);
    }
    flags &= ~FLAG_REBUILD_VARS;
  }

  public Block copy() {
    return new Block(rules.copy(), flags);
  }

  public String dumpDefs() {
    Buffer buf = new Buffer(4);
    dumpDefs(buf);
    return buf.toString();
  }

  /**
   * Debug method - collects list of definitions inside this block.
   */
  public boolean dumpDefs(Buffer buf) {
    boolean output = false;
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (node.is(NodeType.DEFINITION)) {
        String repr = node.toString().replaceAll("\\s+", " ");
        buf.indent().append(repr).append('\n');
        output = true;
      }
    }
    return output;
  }

  @Override
  public NodeType type() {
    return NodeType.BLOCK;
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

  @Override
  public void modelRepr(Buffer buf) {
    if (rules != null) {
      ReprUtils.modelRepr(buf, "\n", true, rules);
    }
  }

  public void orFlags(Block block) {
    flags |= block.flags;
  }

  private void setFlags(Node node) {
    if (node instanceof Import) {
      flags |= FLAG_HAS_IMPORTS;
    } else if (node instanceof MixinCall) {
      flags |= FLAG_HAS_MIXIN_CALLS;
    }
  }

}
