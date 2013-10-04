package com.squarespace.v6.template.less.model;

import java.util.HashMap;
import java.util.Map;

import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.FlexList;
import com.squarespace.v6.template.less.core.LessUtils;


/**
 * Represents an array of rules which can be evaluated. A given rule,
 * such an IMPORT or MIXIN_CALL can be expanded into multiple rules which
 * will be spliced into the array.  For this reason, blocks manage their
 * arrays of objects directly.
 */
public class Block extends BaseNode {

  private static final int INITIAL_CAPACITY = 8;
  
  private FlexList<Node> rules;
  
  private Directive charset;

  private Map<String, Definition> variables;
  
  private boolean rebuildVariables = true;
  
  public Block() {
    this(INITIAL_CAPACITY);
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
    rules.splice(0, 0, new Node[] { node });
  }

  public void appendNode(Node node) {
    rules.append(node);
  }

  public void appendBlock(Block block) {
    rules.append(block.rules);
  }

  public FlexList<Node> rules() {
    return rules;
  }
  
  public void resetCache() {
    rebuildVariables = true;
  }
  
  public Definition resolveDefinition(String name) {
    if (rebuildVariables) {
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
    rebuildVariables = false;
  }
  
  public Block copy() {
    Block result = new Block();
    result.rules = rules.copy();
    return result;
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
  public String toString() {
    return rules.toString();
  }
  
  @Override
  public void repr(Buffer buf) {
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node rule = rules.get(i);
      buf.indent();
      rule.repr(buf);
    }
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    if (rules != null) {
      ReprUtils.modelRepr(buf, "\n", true, rules);
    }
  }
  
}
