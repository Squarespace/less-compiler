package com.squarespace.v6.template.less.model;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.exec.ExecEnv;


public interface Node {

  public NodeType type();

  public int lineOffset();

  public int charOffset();
  
  /**
   * Outputs the original LESS representation of this node.
   */
  public void repr(Buffer buf);

  public String repr();
  
  /**
   * Outputs a human-readable representation of the model node.
   */
  public void modelRepr(Buffer buf);
  
  
  /**
   * Evaluates the node against the Frame and returns itself or a new Node
   * that is itself an Atom, e.g. reduced to its simplest form possible.
   */
  public Node eval(ExecEnv env) throws LessException;

  /**
   * 
   */
//  public void render(Env env, CssBuffer buf) throws LessException;

  public boolean is(NodeType type);
  
  /**
   * Optimization to avoid eval() when the node is an Atom or a composite
   * that contains no variable references.
   */
  public boolean needsEval();

//  // XXX: replace with node groups
//  public boolean isBlock();

  /**
   * Called when a node participates on an operation.
   */
  public Node operate(Operator op, Node arg) throws LessException;

  public void setLineOffset(int offset);
  
  public void setCharOffset(int offset);

}
