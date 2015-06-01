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

package com.squarespace.less.parse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.squarespace.less.FilesystemLessLoader;
import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessLoader;
import com.squarespace.less.exec.ImportRecord;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Url;


/**
 * Handles resolution of import paths.
 */
public class Importer {

  private static final Pattern IMPORT_EXT = Pattern.compile(".*(\\.[a-z]*$)|([\\?;].*)$");

  private static final Pattern IMPORT_CSS = Pattern.compile(".*css([\\?;].*)?$");

  private final Map<Path, ImportRecord> importRecords = new HashMap<>();

  private final LessContext context;

  private final LessLoader loader;

  public Importer(LessContext context) {
    this(context, null);
  }

  public Importer(LessContext context, LessLoader loader) {
    this.context = context;
    this.loader = (loader == null) ? new FilesystemLessLoader() : loader;
  }

  /**
   * Resolve the path and determine if it can be loaded. Returns the resolved
   * path or null if no valid path could be resolved.
   */
  public Path resolvePath(Import importNode) throws LessException {
    String rawPath = renderImportPath(importNode);
    return resolve(importNode.rootPath(), rawPath);
  }

  /**
   * Records that an import occurred for a given path. This is used to enforce
   * the "once" directive globally.
   */
  public void recordImport(Import importNode, Path path) {
    importRecords.put(path, new ImportRecord(path, null, importNode.once()));
  }

  /**
   * Load the source from the {@link LessLoader}.
   */
  public String loadSource(Path path) throws LessException {
    return loader.load(path);
  }

  /**
   * Indicate whether the import for the given path should be suppressed.
   */
  public boolean shouldSuppressImport(Path path) {
    ImportRecord record = importRecords.get(path);
    if (record != null) {
      // We need to suppress this import if one of the following is true:
      //  1. A record exists and global importOnce() flag is set
      //  2. This record is marked "only once"
      //
      if (context.options().importOnce() || record.onlyOnce()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Resolve the path and determine if it can be loaded. Returns the resolved
   * path or null if no valid path could be resolved.
   */
  private Path resolve(Path rootPath, String rawPath) {
    if (rawPath == null) {
      return null;
    }

    Path path = Paths.get(rawPath);

    // First, check if the path is a sibling of the current stream's input.
    if (rootPath != null) {
      path = loader.normalize(rootPath.resolve(rawPath));
      if (loader.exists(path)) {
        return path;
      }
    }

    // If no global import paths are defined, return.
    List<Path> importPaths = context.options().importPaths();
    if (importPaths == null || importPaths.isEmpty()) {
      return null;
    }

    // Check if the path exists under one of the global import paths,
    // if any are defined.
    for (Path importPath : importPaths) {
      path = loader.normalize(importPath.resolve(rawPath));
      if (loader.exists(path)) {
        return path;
      }
    }

    // Nothing resolved.
    return null;
  }

  /**
   * Convert the import node's path into a String.
   */
  private String renderImportPath(Import importNode) throws LessException {
    Node node = importNode.path();
    if (node instanceof Url) {
      return null;
    }

    String path = null;
    if (node instanceof Quoted) {
      Quoted quoted = ((Quoted)node).copy();
      quoted.setEscape(true);
      node = quoted;
    }

    path = context.render(node);
    Matcher matcher = IMPORT_EXT.matcher(path);
    if (!matcher.matches()) {
      // Append optional ".less" extension
      path += ".less";
    } else {
      matcher = IMPORT_CSS.matcher(path);
      if (matcher.matches()) {
        return null;
      }
    }
    return path;
  }

}
