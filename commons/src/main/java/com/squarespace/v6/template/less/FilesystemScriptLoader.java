package com.squarespace.v6.template.less;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import com.squarespace.v6.template.less.core.ErrorMaker;


public class FilesystemScriptLoader implements ScriptLoader {

  @Override
  public String load(Path path) throws LessException {
    return readFile(path);
  }

  private String readFile(Path path) throws LessException {
    try (InputStream input = Files.newInputStream(path)) {
      return IOUtils.toString(input);
      
    } catch (IOException e) {
      throw new LessException(ErrorMaker.importError(path, e.getMessage()));
    }
  }

}
