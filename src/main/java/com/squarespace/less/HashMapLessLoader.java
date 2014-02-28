package com.squarespace.less;

import static com.squarespace.less.core.ExecuteErrorMaker.importError;

import java.nio.file.Path;
import java.util.Map;


public class HashMapLessLoader implements LessLoader {

  private Map<Path, String> storage;
  
  public HashMapLessLoader(Map<Path, String> storage) {
    this.storage = storage;
  }
  
  @Override
  public boolean exists(Path path) {
    return storage.containsKey(path);
  }

  @Override
  public String load(Path path) throws LessException {
    String result = storage.get(path);
    if (result == null) {
      throw new LessException(importError(path, "File cannot be found"));
    }
    return result;
  }
  
}
