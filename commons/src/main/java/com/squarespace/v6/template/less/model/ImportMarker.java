package com.squarespace.v6.template.less.model;


/**
 * A dummy node which is placed into a block to indicate where an IMPORT
 * statements imported rules begins and ends.
 */
public class ImportMarker extends BaseNode {

  private Import importStatement;
  
  private boolean beginning;
  
  public ImportMarker(Import imp, boolean begin) {
    this.importStatement = imp;
    this.beginning = begin;
  }
  
  public Import importStatement() {
    return importStatement;
  }

  public boolean beginning() {
    return beginning;
  }
  
  @Override
  public NodeType type() {
    return NodeType.IMPORT_MARKER;
  }
  
}
