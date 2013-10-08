package com.squarespace.v6.template.less;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.core.LessUtils;


public abstract class BaseCommand {

  protected static final String GLOB_LESS = "glob:*.less";
  
  protected static final String VERSION = "1.3.3";

  protected static final String SEPARATOR = 
      "\n==============================================================================\n";

  protected List<Path> getMatchingFiles(Path rootPath, String globPattern) throws IOException {
    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(globPattern);
    DirectoryStream<Path> dirStream = LessUtils.getMatchingFiles(rootPath, matcher);
    List<Path> result = new ArrayList<>();
    for (Path path : dirStream) {
      result.add(path);
    }
    return result;
  }
  
}
