package com.squarespace.less;

import java.nio.file.Path;


/**
 * Interface for loading external data from a given path.
 */
public interface LessLoader {

  /** Peek to see if the target file exists before trying to load it */
  boolean exists(Path path);

  /** Load the target file */
  String load(Path path) throws LessException;

}
