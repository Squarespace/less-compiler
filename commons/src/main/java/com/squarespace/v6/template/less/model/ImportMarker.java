package com.squarespace.v6.template.less.model;


/**
 * A dummy node which is placed into a block to indicate where an IMPORT
 * statements imported rules begins and ends.
 */
public class ImportMarker extends BaseNode {

  private Import importStatement;
  
  public ImportMarker(Import imp) {
    this.importStatement = imp;
  }
  
  public Import importStatement() {
    return importStatement;
  }
  
  @Override
  public NodeType type() {
    return NodeType.IMPORT_MARKER;
  }
  
}
