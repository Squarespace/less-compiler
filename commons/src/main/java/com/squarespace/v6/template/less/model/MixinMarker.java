package com.squarespace.v6.template.less.model;


/**
 * A dummy node which is placed into a block to indicate where a MIXIN
 * call's generated nodes begin and end.
 */
public class MixinMarker extends BaseNode {

  private MixinCall call;
  
  private boolean beginning;
  
  public MixinMarker(MixinCall call, boolean begin) {
    this.call = call;
    this.beginning = begin;
  }
  
  public MixinCall mixinCall() {
    return call;
  }

  public boolean beginning() {
    return beginning;
  }
  
  @Override
  public NodeType type() {
    return NodeType.MIXIN_MARKER;
  }

}
