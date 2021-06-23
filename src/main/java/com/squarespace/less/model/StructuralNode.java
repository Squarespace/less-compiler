package com.squarespace.less.model;

import com.squarespace.less.core.Buffer;

/**
 * A node that performs a key structural role in the stylesheet, forming the skeleton.
 * We are able to associate position information (line and column) and user data
 * with these nodes.
 */
public abstract class StructuralNode implements Node, HasUserData {

  /**
   * Line offset where this node was defined.
   */
  protected int lineOffset;

  /**
   * Character offset of the line where this node was defined.
   */
  protected int charOffset;

  /**
   * Estimated node size, in characters.
   */
  protected int size;

  /**
   * User data attached to this node
   */
  protected Object userData;

  /**
   * Indicates if this is a structural node.
   */
  @Override
  public boolean isStructural() {
    return true;
  }

  @Override
  public boolean hasUserData() {
    return true;
  }

  /**
   * Returns any user data attached to this node.
   */
  public Object userData() {
    return userData;
  }

  /**
   * Attaches the user data to this node.
   */
  public void userData(Object userData) {
    this.userData = userData;
  }

  /**
   * Copies the base field values of {@code from} to this node.
   */
  public void copyStructure(StructuralNode from) {
    setLineOffset(from.lineOffset);
    setCharOffset(from.charOffset);
    userData(from.userData);
  }

  /**
   * Returns the line offset where this node was defined.
   */
  public int lineOffset() {
    return lineOffset;
  }

  /**
   * Sets the line offset where this node was defined.
   */
  public void setLineOffset(int lineOffset) {
    this.lineOffset = lineOffset;
  }

  /**
   * Returns the character offset of the line where this node was defined.
   */
  public int charOffset() {
    return charOffset;
  }

  /**
   * Sets the character offset of the line where this node was defined.
   */
  public void setCharOffset(int charOffset) {
    this.charOffset = charOffset;
  }

  /**
   * Returns the nodes estimated size, in characters.
   */
  public int size() {
    return size;
  }

  /**
   * Sets the stimated size for this node.
   */
  public void setSize(int size) {
    this.size = size;
  }

  /**
   * Constructs the position representation for this node.
   */
  public void posRepr(Buffer buf) {
    buf.append(" [").append(lineOffset).append(',').append(charOffset).append("]");
  }

  @Override
  public String toString() {
    return ModelUtils.toString(this);
  }

  @Override
  public int hashCode() {
    return ModelUtils.notHashable();
  }
}
