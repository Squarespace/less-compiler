package com.squarespace.v6.template.less;

import java.nio.file.Path;


public interface ScriptLoader {

  public String load(Path path) throws LessException;
  
}
