package com.squarespace.v6.template.less.exec;

import com.squarespace.v6.template.less.model.MixinParams;
import com.squarespace.v6.template.less.model.Node;


public class MixinMatch {

  private Node mixin;
  
  private MixinParams params;
  
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
