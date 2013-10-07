package com.squarespace.v6.template.less;

import java.nio.file.Path;


/**
 * Interface for loading external data from a given path.
 */
public interface LessLoader {

  public String load(Path path) throws LessException;
  
}
