package com.squarespace.less.core;

import static com.squarespace.less.core.ExecuteErrorMaker.importError;

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
import com.squarespace.less.model.Import;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Stylesheet;


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
   */
  public void importStylesheet(Import importNode) throws LessException {
    String rawPath = renderImportPath(importNode);
    Stylesheet sheet = importStylesheet(rawPath, importNode.rootPath(), importNode.once());
    importNode.block(sheet.block());
  }

  /**
   * Retrieves an external stylesheet. If not already cached, parse it and cache it.
   */
  public Stylesheet importStylesheet(String rawPath, Path rootPath, boolean once) throws LessException {
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
    // once and the flag is enforced.
    if (record != null) {

      // Global "import once" flag. All imports are processed only once.
      if (context.options().importOnce()) {
        return null;
      }

      if (!record.onlyOnce()) {
        context.stats().importDone(true);
      }
      return record.onlyOnce() ? null : record.stylesheeet().copy();
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
    if (node.is(NodeType.URL)) {
      return null;
    }
    String path = null;
    if (node.is(NodeType.QUOTED)) {
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
