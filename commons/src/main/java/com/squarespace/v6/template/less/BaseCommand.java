package com.squarespace.v6.template.less;

import java.io.IOException;
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
import java.util.List;

import com.squarespace.v6.template.less.core.LessUtils;


public abstract class BaseCommand {

  protected static final String GLOB_LESS = "glob:*.less";
  
  protected static final String VERSION = "1.3.3";

  protected static final String SEPARATOR = 
      "\n==============================================================================\n";

  protected List<Path> getMatchingFiles(final Path rootPath, String globPattern, boolean recursive) throws IOException {
    final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
    final List<Path> result = new ArrayList<>();
    if (!recursive) {
      DirectoryStream<Path> dirStream = LessUtils.getMatchingFiles(rootPath, matcher);
      for (Path path : dirStream) {
        result.add(path);
      }

    } else {
      FileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
          if (matcher.matches(file.getFileName())) {
            result.add(file);
          }
          return FileVisitResult.CONTINUE;
        }
      };
      Files.walkFileTree(rootPath, visitor);
    }
    return result;
  }

}
