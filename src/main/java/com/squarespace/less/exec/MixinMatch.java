package com.squarespace.less.exec;

import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;


/**
 * Represents a single match found during mixin resolution.
 */
public class MixinMatch {

  private final Node mixin;
  
  private final MixinParams params;
  
  public MixinMatch(Node mixin, MixinParams params) {
    this.mixin = mixin;
    this.params = params;
  }
  
  public Node mixin() {
    return mixin;
  }
  
  public MixinParams params() {
    return params;
  }

}
