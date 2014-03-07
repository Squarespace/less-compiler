package com.squarespace.less;

import static com.squarespace.less.core.ExecuteErrorMaker.importError;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;

import com.squarespace.less.core.Constants;


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
      return IOUtils.toString(input, Constants.UTF8);

    } catch (NoSuchFileException e) {
      throw new LessException(importError(path, "File cannot be found"));

    } catch (IOException e) {
      throw new LessException(importError(path, e.getMessage()));
    }
  }

}
