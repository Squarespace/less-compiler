package com.squarespace.v6.template.less.exec;

import com.squarespace.v6.template.less.model.Stylesheet;


/**
 * Associates an imported stylesheet with the 'onlyOnce' flag value set when it was imported.
 */
public class ImportRecord {

  private Stylesheet stylesheet;
  
  private boolean onlyOnce;
  
  public ImportRecord(Stylesheet stylesheet, boolean onlyOnce) {
    this.stylesheet = stylesheet;
    this.onlyOnce = onlyOnce;
  }
  
  public Stylesheet stylesheeet() {
    return stylesheet;
  }
  
  public boolean onlyOnce() {
    return onlyOnce;
  }
  
}
