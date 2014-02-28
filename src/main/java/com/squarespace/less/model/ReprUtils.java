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
