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

package com.squarespace.less.core;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Node;


/**
 * Generic utility methods.
 */
public class LessUtils {

  private static final int COPY_BUFFER_SIZE = 1024 * 8;

  private LessUtils() {
  }

  /**
   * Null checks the object references before calling {@link Object#equals(Object)}.
   */
  public static boolean safeEquals(Object o1, Object o2) {
    return (o1 == null) ? (o1 == o2) : o1.equals(o2);
  }

  /**
   * Ensures that a valid list instance is always returned.
   */
  public static <T> List<T> safeList(List<T> list) {
    return list == null ? Collections.<T>emptyList() : list;
  }

  /**
   * Initializes a list if necessary, with {@code initialSize}.
   */
  public static <T> List<T> initList(List<T> list, int initialSize) {
    if (list == null) {
      list = new ArrayList<T>(initialSize);
    }
    return list;
  }

  /**
   * Initializes a map if necessary, with {@code initialSize}.
   */
  public static <K, V> Map<K, V> initHashMap(Map<K, V> map, int initialSize) {
    if (map == null) {
      map = new HashMap<K, V>(initialSize);
    }
    return map;
  }

  /**
   * Reads a UTF8-encoded file into a string.
   */
  public static String readFile(Path path) throws IOException {
    try (InputStream file = Files.newInputStream(path)) {
      return readStream(file);
    }
  }

  /**
   * Reads a UTF-8-encoded string into a string.
   */
  public static String readStream(InputStream stream) throws IOException {
    try (InputStreamReader reader = new InputStreamReader(stream, Constants.UTF8)) {
      return readToString(reader);
    }
  }

  /**
   * Writes a string to a file using UTF8 encoding.
   */
  public static void writeFile(Path outPath, String data) throws IOException {
    try (OutputStream output = Files.newOutputStream(outPath, CREATE, TRUNCATE_EXISTING)) {
      output.write(data.getBytes(Constants.UTF8));
    }
  }

  /**
   * Flattens the list types {@link Expression} and {@link ExpressionList} of length 1
   * by returning their first value
   */
  public static Node flatten(Node node) {
    List<Node> values = listValues(node);
    if (values == null) {
      return node;
    }
    return values.size() == 1 ? values.get(0) : node;
  }

  /**
   * If the node is one of the list types, return the values; otherwise return null.
   */
  public static List<Node> listValues(Node node) {
    if (node instanceof Expression) {
      return ((Expression)node).values();
    } else if (node instanceof ExpressionList) {
      return ((ExpressionList)node).expressions();
    }
    return null;
  }

  /**
   * Returns a list of all files matching the given pattern. If {@code recursive} is true
   * will recurse into all directories below {@code rootPath}.
   */
  public static List<Path> getMatchingFiles(final Path rootPath, String globPattern, boolean recursive)
      throws IOException {
    final List<Path> result = new ArrayList<>();
    if (!recursive) {
      DirectoryStream<Path> dirStream = getMatchingFiles(rootPath, globPattern);
      for (Path path : dirStream) {
        result.add(path);
      }

    } else {
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
      FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
          if (matcher.matches(file.getFileName())) {
            result.add(rootPath.relativize(file));
          }
          return FileVisitResult.CONTINUE;
        }
      };
      Files.walkFileTree(rootPath, visitor);
    }
    return result;
  }

  public static DirectoryStream<Path> getMatchingFiles(Path root, String pattern) throws IOException {
    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(pattern);
    return Files.newDirectoryStream(root, new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(Path entry) throws IOException {
        return !Files.isDirectory(entry) && matcher.matches(entry.getFileName());
      }
    });
  }

  /**
   * Builds a string containing the names of all enumerated values of the given type.
   */
  public static <T extends Enum<T>> String enumValueList(Class<T> enumType, boolean lowercase) {
    StringBuilder buf = new StringBuilder();
    T[] constants = enumType.getEnumConstants();
    int size = constants.length;
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(", ");
      }
      String name = constants[i].name();
      buf.append(lowercase ? name.toLowerCase() : name);
    }

    return buf.toString();
  }

  /**
   * Read data from the {@code input} into a string.
   */
  public static String readToString(Reader input) throws IOException {
    StringBuilder output = new StringBuilder();
    char[] temp = new char[COPY_BUFFER_SIZE];
    for (;;) {
      int n = input.read(temp);
      if (n == -1) {
        break;
      }
      output.append(temp, 0, n);
    }
    return output.toString();
  }

  /**
   * Extracts a resource from a JAR into a temporary directory. The JAR URL
   * must point to the full path to the resource.
   *
   * Example:   jar:file:/path/to/file.jar!/my-resource/
   *
   * The method of splitting the JAR/resource paths using the "!/" pattern
   * is essentially how the Sun URLConnection class parses it.
   */
  public static Path extractTempJarResource(URL jarUrl) throws IOException {

    // Locate the boundary between JAR and resource paths, and split on the boundary.
    String rawPath = jarUrl.getPath().replaceFirst("file:", "");
    int n = rawPath.indexOf("!/");

    if (n == -1) {
      throw new IOException("bad url for jar: no '!/' found in " + jarUrl);
    }

    String jarPath = rawPath.substring(0, n);
    String filePath = rawPath.substring(n + 2);

    // Setup a temporary directory to extract the files into, and ensure it gets
    // automagically cleaned up.
    Path tempDir = Files.createTempDirectory("less-core-temp-");
    tempDir.toFile().deleteOnExit();

    // Extract the files and return the full path to the extracted resource.
    try (JarInputStream stream = new JarInputStream(new FileInputStream(jarPath))) {
      while (true) {
        JarEntry entry = stream.getNextJarEntry();
        if (entry == null) {
          break;
        }

        // Only extract resources starting with our desired path.
        String name = entry.getName();
        if (!name.startsWith(filePath)) {
          continue;
        }

        // Create dirs / copy files.
        Path tempPath = tempDir.resolve(name);
        if (entry.isDirectory()) {
          tempPath.toFile().mkdirs();
        } else {
          Files.copy(stream, tempPath);
        }
        stream.closeEntry();
      }
    }

    // Return full path to extracted temporary resource.
    return tempDir.resolve(filePath);
  }

}
