package com.squarespace.less.model;

import static com.squarespace.less.core.ExecuteErrorMaker.varUndefined;
import static com.squarespace.less.core.LessUtils.safeEquals;
import static com.squarespace.less.model.NodeType.VARIABLE;

import com.squarespace.less.Context;
import com.squarespace.less.LessException;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.exec.ExecEnv;


/**
 * Represents a reference to a variable.
 */
public class Variable extends BaseNode {

  private final String name;

  private final boolean indirect;

  private final boolean curly;

  public Variable(String name) {
    this(name, false);
  }

  public Variable(String name, boolean curly) {
    if (name == null) {
      throw new LessInternalException("Serious error: name cannot be null");
    }
    if (name.startsWith("@@")) {
      name = name.substring(1);
      indirect = true;
    } else {
      indirect = false;
    }
    this.name = name;
    this.curly = curly;
  }

  public String name() {
    return name;
  }

  public boolean curly() {
    return curly;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Variable) {
      Variable other = (Variable)obj;
      return indirect == other.indirect
          && curly == other.curly
          && safeEquals(name, other.name);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean needsEval() {
    return true;
  }

  @Override
  public Node eval(ExecEnv env) throws LessException {
    Definition def = (Definition) env.resolveDefinition(name);
    if (def == null) {
      throw new LessException(varUndefined(name));
    }

    Node result = def.dereference(env);
    if (!indirect) {
      return result;
    }

    // Render the node to obtain the new variable name and eval that. We render
    // the value as if it were inside a string.
    Context ctx = env.context();
    Buffer buf = ctx.newBuffer();
    buf.startDelim('"');
    ctx.render(buf, result);
    return new Variable("@" + buf.toString()).eval(env);
  }

  @Override
  public NodeType type() {
    return VARIABLE;
  }

  @Override
  public void repr(Buffer buf) {
    if (indirect) {
      buf.append('@');
    }
    buf.append('@');
    if (curly) {
      buf.append('{');
    }
    buf.append(name.substring(1));
    if (curly) {
      buf.append('}');
    }
  }

  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(indirect ? "@" + name : name);
    if (curly) {
      buf.append(" (curly)");
    }
  }

}
