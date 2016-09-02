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

import static com.squarespace.less.core.SyntaxErrorMaker.importError;

import java.nio.file.Path;
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
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.ImportMarker;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.Url;


/**
 * Handles importing and caching stylesheets.
 */
public class LessImporter {

  private static final Pattern IMPORT_EXT = Pattern.compile(".*(\\.[a-z]*$)|([\\?;].*)$");

  private static final Pattern IMPORT_CSS = Pattern.compile(".*css([\\?;].*)?$");

  private final Map<Path, ImportRecord> importCache = new HashMap<>();

  private final LessContext context;

  private final LessLoader loader;

  private final Map<Path, Stylesheet> preCache;

  public LessImporter(LessContext ctx, LessLoader loader, Map<Path, Stylesheet> preCache) {
    this.context = ctx;
    this.loader = (loader == null) ? new FilesystemLessLoader() : loader;
    this.preCache = (preCache == null) ? new HashMap<Path, Stylesheet>() : preCache;
  }

  /**
   * Retrieves an external stylesheet and initializes the import node's block.
   * If not already cached, parse it and cache it.
   */
  public Node importStylesheet(Import importNode) throws LessException {
    String rawPath = renderImportPath(importNode);
    if (rawPath == null) {
      return importNode;
    }

    // Mark import recursion start.
    context.enterImport();

    int limit = context.options().importRecursionLimit();
    if (context.importDepth() > limit) {
      throw new LessException(importError(rawPath, "Recursion limit of " + limit + " exceeded"));
    }

    Stylesheet sheet = importStylesheet(rawPath, importNode);
    if (sheet == null) {
      // When import-once is used, we disappear the import node.
      context.exitImport();
      return new Block(0);
    }

    Block block = sheet.block();
    Features features = importNode.features();
    if (features != null && !features.isEmpty()) {
      Media media = new Media(features, block);
      block = new Block();
      block.appendNode(media);
    }
    if (context.options().tracing()) {
      block.prependNode(new ImportMarker(importNode, true));
      block.appendNode(new ImportMarker(importNode, false));
    }
    context.exitImport();
    return block;
  }

  /**
   * Retrieves an external stylesheet.
   */
  public Stylesheet importStylesheet(String rawPath, Import importNode) throws LessException {
    Path rootPath = importNode.rootPath();
    boolean once = importNode.once();
    List<Path> importPaths = context.options().importPaths();
    ImportRecord record = null;
    Path path = null;

    if (rootPath != null) {
      path = rootPath.resolve(rawPath).toAbsolutePath().normalize();
      record = importCache.get(path);
    }

    // If not found relative to the sibling dir, search the import path.
    if (record == null && importPaths != null && !importPaths.isEmpty()) {
      for (Path importPath : importPaths) {
        path = importPath.resolve(rawPath).toAbsolutePath().normalize();
        record = importCache.get(path);
        if (record != null) {
          break;
        }
      }
    }

    // If the stylesheet has been imported and the 'onlyOnce' flag is not set, return it.
    // Otherwise return null, indicating to the caller that it has already been imported
    // once and the flag is being enforced.
    if (record != null) {

      // If either the global or per-node "once" flag is set, suppress this import node
      // in the output.
      if (context.options().importOnce() || record.onlyOnce()) {
        importNode.suppress(true);
        return null;
      }

      context.stats().importDone(true);
      return record.stylesheeet().copy();
    }

    // If a pre-populated parsed stylesheet cache has been provided, use it.
    Stylesheet result = null;
    if (preCache != null) {
      if (rootPath != null) {
        path = rootPath.resolve(rawPath).toAbsolutePath().normalize();
        result = preCache.get(path);
      }
      if (result == null && importPaths != null && !importPaths.isEmpty()) {
        for (Path importPath : importPaths) {
          path = importPath.resolve(rawPath).toAbsolutePath().normalize();
          result = preCache.get(path);
          if (result != null) {
            break;
          }
        }
      }
    }

    // Else, ask the loader if the file exists and parse it.
    if (result == null) {
      path = resolvePath(rootPath, rawPath);
      if (path == null) {
        throw new LessException(importError(rawPath, "File cannot be found"));
      }
      result = context.compiler().parse(loader.load(path), context, path.getParent(), path.getFileName());
    }

    // Stick it in the cache if not already present.
    if (!importCache.containsKey(path)) {
      importCache.put(path, new ImportRecord(path, result, once));
    }
    context.stats().importDone(false);
    return result.copy();
  }

  /**
   * Search the rootPath and the importPaths if any, looking for a file that exists.
   */
  private Path resolvePath(Path rootPath, String rawPath) {
    Path path = null;
    if (rootPath != null) {
      path = rootPath.resolve(rawPath).toAbsolutePath().normalize();
      if (loader.exists(path)) {
        return path;
      }
    }
    List<Path> importPaths = context.options().importPaths();
    if (importPaths == null || importPaths.isEmpty()) {
      return null;
    }
    for (Path importPath : importPaths) {
      path = importPath.resolve(rawPath).toAbsolutePath().normalize();
      if (loader.exists(path)) {
        return path;
      }
    }
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
