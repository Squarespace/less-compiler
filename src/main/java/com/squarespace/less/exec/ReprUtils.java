package com.squarespace.less.exec;

import java.util.ArrayList;
import java.util.List;

import com.squarespace.less.model.Node;


/**
 * Utility methods to help in generating and selecting lines from a Node instances repr() value.
 */
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
