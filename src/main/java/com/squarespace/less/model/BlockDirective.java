package com.squarespace.less.model;

import static com.squarespace.less.core.LessUtils.safeEquals;

import com.squarespace.less.core.Buffer;


public class BlockDirective extends BlockNode {

  private final String name;

  public BlockDirective(String name, Block block) {
    this.name = name;
    setBlock(block);
  }

  public String name() {
    return name;
  }

  public BlockDirective copy() {
    BlockDirective result = new BlockDirective(name, block.copy());
    result.fileName = fileName;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BlockDirective) {
      BlockDirective other = (BlockDirective)obj;
      return safeEquals(name, other.name) && super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public NodeType type() {
    return NodeType.BLOCK_DIRECTIVE;
  }

  @Override
  public void repr(Buffer buf) {
    buf.append(name);
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
    buf.incrIndent().indent();
    super.modelRepr(buf);
    buf.decrIndent();
  }

}
