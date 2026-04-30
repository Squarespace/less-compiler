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

import static com.squarespace.less.core.SyntaxErrorMaker.importError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.squarespace.less.model.Import;


/**
 * Forces all imports to be children of a given filesystem root.
 *
 * No {@link Import} instruction will be allowed to read files outside this root.
 * It also restricts the file extensions of imports to those matching the "\.(less|css)$"
 * pattern.
 */
public class JailedFilesystemLessLoader extends FilesystemLessLoader {

  /**
   * Pattern to ensure import paths end in a legal extension.
   */
  private static final Pattern ACCEPT_IMPORT = Pattern.compile(".*\\.(less|css)$");

  /**
   * Parent path. All imported paths must be children of this path.
   */
  private final Path jailRoot;

  public JailedFilesystemLessLoader(Path jailRoot) {
    this.jailRoot = jailRoot.toAbsolutePath().normalize();
  }

  @Override
  public boolean exists(Path path) {
    if (!ACCEPT_IMPORT.matcher(path.getFileName().toString()).matches()) {
      return false;
    }
    Path real = resolveContained(path);
    return real != null && Files.exists(real);
  }

  @Override
  public String load(Path path) throws LessException {
    // Resolve once and read the resolved target, so the containment
    // check and the read see the same file.
    Path real = resolveContained(path);
    if (real == null) {
      throw new LessException(importError(path, "File cannot be found"));
    }
    return super.load(real);
  }

  /**
   * Resolve the path inside the jail, following symlinks, and return its
   * real path only if the final target is still inside the jail root.
   * Returns null when the file is missing, is a broken link, or the
   * resolved target escapes the jail.
   */
  private Path resolveContained(Path path) {
    Path candidate = jailRoot.resolve(path).toAbsolutePath().normalize();
    if (!candidate.startsWith(jailRoot)) {
      return null;
    }
    try {
      Path real = candidate.toRealPath();
      if (!real.startsWith(realJailRoot())) {
        return null;
      }
      return real;
    } catch (IOException e) {
      // Missing file or broken link.
      return null;
    }
  }

  /**
   * Real path of the jail root. The root itself may sit behind symlinks
   * (e.g. /tmp on macOS), so containment compares real paths on both
   * sides. Falls back to the lexical root when it cannot be resolved.
   */
  private Path realJailRoot() {
    try {
      return jailRoot.toRealPath();
    } catch (IOException e) {
      return jailRoot;
    }
  }

}
