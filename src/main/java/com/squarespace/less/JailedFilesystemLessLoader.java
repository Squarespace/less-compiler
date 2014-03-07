package com.squarespace.less;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Forces all imports to be resolved against a jailed root. No import
 * will be allowed to read files above this path. It also restricts the
 * file extensions of imports to those matching the "\.(less|css)$" regex.
 */
public class JailedFilesystemLessLoader extends FilesystemLessLoader {

  private static final Pattern ACCEPT_IMPORT = Pattern.compile(".*\\.(less|css)$");

  private final Path jailRoot;

  public JailedFilesystemLessLoader(Path jailRoot) {
    this.jailRoot = jailRoot.toAbsolutePath().normalize();
  }

  @Override
  public boolean exists(Path path) {
    Matcher matcher = ACCEPT_IMPORT.matcher(path.getFileName().toString());
    if (!matcher.matches()) {
      return false;
    }
    Path tempPath = jailRoot.resolve(path).toAbsolutePath().normalize();
    if (!tempPath.startsWith(jailRoot)) {
      return false;
    }
    return Files.exists(tempPath);
  }

}
