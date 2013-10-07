package com.squarespace.v6.template.less.exec;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.v6.template.less.model.Node;


public class ReprUtils {

  private ReprUtils() {
  }
  
  public static List<String> reprLines(Node node) {
    return reprLines(node, -1);
  }
  
  public static List<String> reprLines(Node node, int limit) {
    return splitLines(node.repr(), limit);
  }
  
  public static List<String> splitLines(String raw, int limit) {
    String[] lines = raw.split("\n");
    List<String> result = new ArrayList<>();
    int count = 0;
    for (String line : lines) {
      line = line.trim();
      if (!line.isEmpty()) {
        if (count > 0 && count == limit) {
          break;
        }
        result.add(line);
        count++;
      }
    }
    return result;
  }

}
