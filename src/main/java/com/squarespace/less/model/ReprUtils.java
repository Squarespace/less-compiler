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

package com.squarespace.less.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.FlexList;


public class ReprUtils {

  private ReprUtils() {
  }

  public static void modelRepr(Buffer buf, Collection<? extends Node> nodes) {
    modelRepr(buf, null, false, nodes);
  }

  public static void modelRepr(Buffer buf, String delim, List<? extends Node> nodes) {
    modelRepr(buf, delim, false, nodes);
  }

  public static void modelRepr(Buffer buf, String delim, boolean indent, Collection<? extends Node> nodes) {
    if (nodes != null) {
      Iterator<? extends Node> iter = nodes.iterator();
      while (iter.hasNext()) {
        if (indent) {
          buf.indent();
        }
        Node next = iter.next();
        if (next != null) {
          next.modelRepr(buf);
          if (iter.hasNext() && delim != null) {
            buf.append(delim);
          }
        }
      }
    }
  }

  public static void modelRepr(Buffer buf, String delim, boolean indent, FlexList<? extends Node> rules) {
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      if (i > 0 && delim != null) {
        buf.append(delim);
      }
      if (indent) {
        buf.indent();
      }
      Node rule = rules.get(i);
      if (rule != null) {
        rule.modelRepr(buf);
      }
    }
  }

}
