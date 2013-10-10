package com.squarespace.v6.template.less.core;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import org.apache.commons.io.IOUtils;


/**
 * Generic utility methods.
 */
public class LessUtils {

  private LessUtils() {
  }
  
  public static boolean safeEquals(Object o1, Object o2) {
    return (o1 == null) ? (o1 == o2) : o1.equals(o2);
  }
  
  public static <T> List<T> safeList(List<T> list) {
    return list == null ? Collections.<T>emptyList() : list;
  }
  
  public static <T> List<T> initList(List<T> list, int initialSize) {
    if (list == null) {
      list = new ArrayList<T>(initialSize);
    }
    return list;
  }
  
  public static <K, V> Map<K, V> initHashMap(Map<K, V> map, int initialSize) {
    if (map == null) {
      map = new HashMap<K, V>(initialSize);
    }
    return map;
  }

  public static String readFile(Path path) throws IOException {
    try (InputStream input = Files.newInputStream(path)) {
      return IOUtils.toString(input);
    }
  }
  
  public static void writeFile(Path outPath, String data) throws IOException {
    try (OutputStream output = Files.newOutputStream(outPath, CREATE, TRUNCATE_EXISTING)) {
      IOUtils.write(data, output);
    }
  }

  public static List<Path> getMatchingFiles(final Path rootPath, String globPattern, boolean recursive) throws IOException {
    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
    final List<Path> result = new ArrayList<>();
    if (!recursive) {
      DirectoryStream<Path> dirStream = getMatchingFiles(rootPath, matcher);
      for (Path path : dirStream) {
        result.add(path);
      }

    } else {
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

  public static DirectoryStream<Path> getMatchingFiles(Path root, final PathMatcher matcher) throws IOException {
    return Files.newDirectoryStream(root, new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(Path entry) throws IOException {
        return !Files.isDirectory(entry) && matcher.matches(entry.getFileName());
      }
    });
  }

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

}
