package com.squarespace.v6.template.less;

import java.nio.file.Path;


/**
 * Interface for loading external data from a given path.
 */
public interface LessLoader {

  /** Peek to see if the target file exists before trying to load it */
  public boolean exists(Path path);

  /** Load the target file */
  public String load(Path path) throws LessException;
  
}
