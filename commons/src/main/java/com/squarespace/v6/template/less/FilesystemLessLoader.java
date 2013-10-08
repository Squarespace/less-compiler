package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.core.ExecuteErrorMaker.importError;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;


/**
 * Loads the raw data for a given path, using the filesystem.
 */
public class FilesystemLessLoader implements LessLoader {

  @Override
  public boolean exists(Path path) {
    return Files.exists(path);
  }
  
  @Override
  public String load(Path path) throws LessException {
    return readFile(path);
  }

  private String readFile(Path path) throws LessException {
    try (InputStream input = Files.newInputStream(path)) {
      return IOUtils.toString(input);
      
    } catch (NoSuchFileException e) {
      throw new LessException(importError(path, "File cannot be found"));
      
    } catch (IOException e) {
      e.printStackTrace();
      throw new LessException(importError(path, e.getMessage()));
    }
  }

}
