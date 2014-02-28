package com.squarespace.less.exec;

import java.nio.file.Path;

import com.squarespace.less.model.Stylesheet;


/**
 * Associates an imported stylesheet with the 'onlyOnce' flag value set when it was imported.
 */
public class ImportRecord {

  private Path exactPath;
  
  private Stylesheet stylesheet;
  
  private boolean onlyOnce;
  
  public ImportRecord(Path exactPath, Stylesheet stylesheet, boolean onlyOnce) {
    this.exactPath = exactPath;
    this.stylesheet = stylesheet;
    this.onlyOnce = onlyOnce;
  }
  
  public Path exactPath() {
    return exactPath;
  }
  
  public Stylesheet stylesheeet() {
    return stylesheet;
  }
  
  public boolean onlyOnce() {
    return onlyOnce;
  }
  
}
