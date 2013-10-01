package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.core.LessUtils.safeEquals;

import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;



/**
 * A Mixin differs from a Ruleset in the following ways:
 *  - it can take zero or more arguments.
 *  - it is only rendered when called.
 */
public class Mixin extends BlockNode {

  private String name;

  private MixinParams params;
  
  private Guard guard;
  
  private ExecEnv closure;
  
  private int entryCount;
  
  public Mixin(String name, MixinParams params, Guard guard) {
    this(name, params, guard, new Block());
  }
  
  public Mixin(String name, MixinParams params, Guard guard, Block block) {
    super(block);
    this.name = name;
    this.params = params;
    this.guard = guard;
  }
  
  public Mixin copy() {
    Mixin result = new Mixin(name, params, guard, block.copy());
    result.closure = closure;
    if (originalBlockNode != null) {
      result.originalBlockNode = originalBlockNode;
    }
    return result;
  }

  public String name() {
    return name;
  }
  
  public MixinParams params() {
    return params;
  }
  
  public Guard guard() {
    return guard;
  }
  
  public int entryCount() {
    return entryCount;
  }

  public void enter() {
    entryCount++;
  }
  
  public void exit() {
    entryCount--;
  }
  
  public ExecEnv closure() {
    return closure;
  }
  
  public void closure(ExecEnv env) {
    this.closure = env.copy();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Mixin) {
      Mixin other = (Mixin)obj;
      return safeEquals(name, other.name)
          && safeEquals(params, other.params)
          && safeEquals(guard, other.guard);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.MIXIN;
  }
  
  @Override
  public void repr(Buffer buf) {
    buf.append(name).append('(');
    if (params != null) {
      params.repr(buf);
    }
    buf.append(')');
    if (guard != null) {
      buf.append(" when ");
      guard.repr(buf);
    }
    buf.append(" {\n");
    buf.incrIndent();
    block.repr(buf);
    buf.decrIndent();
    buf.indent().append("}\n");
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(name).append('\n');
    buf.incrIndent();

    if (params != null) {
      buf.indent();
      params.modelRepr(buf);
      buf.append('\n');
    }
    
    if (guard != null) {
      buf.indent();
      guard.modelRepr(buf);
      buf.append('\n');
    }

    buf.indent();
    super.modelRepr(buf);
    buf.decrIndent().append('\n');
  }
  
}
