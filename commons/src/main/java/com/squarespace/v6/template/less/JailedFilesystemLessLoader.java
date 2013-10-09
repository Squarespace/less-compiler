package com.squarespace.v6.template.less;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// TODO: pre-load all .less files recursively and place their contents into a map.

/**
 * Forces all imports to be resolved against a jailed root. No import
 * will be allowed to read files above this path. It also restricts the
 * file extensions of imports to those matching the "\.(less|css)$" regex.
 */
public class JailedFilesystemLessLoader extends FilesystemLessLoader {

  private static final Pattern ACCEPT_IMPORT = Pattern.compile(".*\\.(less|css)$");
  private Path jailRoot;
  
  public JailedFilesystemLessLoader(Path jailRoot) {
    this.jailRoot = jailRoot;
  }
  
  @Override
  public boolean exists(Path path) {
    Matcher matcher = ACCEPT_IMPORT.matcher(path.getFileName().toString());
    if (!matcher.matches()) {
      return false;
    }
    Path tempPath = jailRoot.resolve(path).normalize();
    if (!tempPath.startsWith(jailRoot)) {
      return false;
    }
    return Files.exists(tempPath);
  }

}
