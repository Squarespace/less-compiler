package com.squarespace.less.model;


public interface HasUserData {

  /**
   * Returns any user data attached to this node.
   */
  Object userData();

  /**
   * Attaches the user data to this node.
   */
  void userData(Object userData);

}
