package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import java.nio.file.Path;
import java.util.List;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.exec.SelectorUtils;


public class MixinCall extends BaseNode {

  private final Selector selector;

  private final List<String> selectorPath;

  private MixinCallArgs args;

  private boolean important;
  
  private Path fileName;
  
  public MixinCall(Selector selector, MixinCallArgs args, boolean important) {
    this.selector = selector;
    this.args = args;
    this.important = important;
    this.selectorPath = SelectorUtils.renderMixinSelector(selector);
  }
  
  public MixinCall copy() {
    MixinCall result = new MixinCall(selector, args, important);
    result.copyPosition(this);
    result.fileName = fileName;
    return result;
  }
  
  public Selector selector() {
    return selector;
  }
  
  public MixinCallArgs args() {
    return args;
  }
  
  public boolean important() {
    return important;
  }
  
  public List<String> path() {
    return this.selectorPath;
  }
  
  public Path fileName() {
    return fileName;
  }
  
  public void args(MixinCallArgs args) {
    this.args = args;
  }
  
  public void fileName(Path fileName) {
    this.fileName = fileName;
  }
  
  @Override
  public boolean equals(Object obj) {
    // 'path' field is derived from the selector, so doesn't need to be included.
    if (obj instanceof MixinCall) {
      MixinCall other = (MixinCall)obj;
      return important == other.important
          && safeEquals(selector, other.selector)
          && safeEquals(args, other.args);
    }
    return false;
  }
  
  @Override
  public NodeType type() {
    return NodeType.MIXIN_CALL;
  }
  
  @Override
  public void repr(Buffer buf) {
    Selectors.reprSelector(buf, selector);
    if (args != null) {
      args.repr(buf);
    }
    buf.append(";\n");
  }
  
  @Override
  public void modelRepr(Buffer buf) {
    typeRepr(buf);
    buf.append(' ').append(important ? "important" : "");
    buf.append(' ').append(selectorPath.toString()).append('\n');
    buf.incrIndent().indent();
    selector.modelRepr(buf);
    if (args != null) {
      buf.append('\n').indent();
      args.modelRepr(buf);
    }
    buf.decrIndent();
  }
  
}
