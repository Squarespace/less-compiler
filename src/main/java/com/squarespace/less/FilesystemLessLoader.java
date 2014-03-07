/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
