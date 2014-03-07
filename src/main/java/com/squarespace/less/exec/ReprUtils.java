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
