package com.squarespace.v6.template.less.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import com.squarespace.v6.template.less.ErrorInfo;
import com.squarespace.v6.template.less.ErrorType;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Import;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinCallArgs;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Selectors;


public class ErrorUtils {

  private static final int POS_COLUMN_WIDTH = 8;
  
  public static ErrorInfo error(ErrorType code) {
    ErrorInfo info = new ErrorInfo(code);
    info.code(code);
    return info;
  }

  public static String formatError(LessException exc) {
    ErrorInfo primaryError = exc.primaryError();
    Node primaryNode = exc.primaryNode();

    Buffer buf = new Buffer(2);
    Deque<Node> errorContext = exc.errorContext();
    Node lastNode = errorContext.getLast();
    int maxPos = lastNode.lineOffset();
    Iterator<Node> iter = errorContext.iterator();
    while (iter.hasNext()) {
      render(buf, iter.next());
      buf.incrIndent();
      if (iter.hasNext()) {
        buf.append('\n').indent().append("...\n");
      }
    }
    buf.append('\n');
    buf.resetIndent();
    if (primaryNode != null) {
      buf.append(primaryNode.type().toString()).append('\n');
    }
    buf.append(primaryError.getMessage());
    return buf.toString();
  }

  private static void render(Buffer buf, Node node) {
    List<String> lines = null;
    switch (node.type()) {

      case FEATURES:
        buf.append(' ');
        append(buf, reprLines(node), "\n", false);
        break;
        
      case IMPORT:
        buf.indent();
        ((Import)node).repr(buf);
        break;
        
      case MEDIA:
        buf.indent().append("@media");
        render(buf, ((Media)node).features());
        buf.append(" {\n");
        break;
      
      case MIXIN_CALL:
        MixinCall call = (MixinCall)node;
        MixinCallArgs args = call.args();
        Selectors selectors = new Selectors(Arrays.asList(call.selector()));
        buf.indent();
        append(buf, reprLines(selectors, 1), " ", false);
        if (args != null) {
          args.repr(buf);
        }
        buf.append(";\n");
        break;
        
      case RULESET:
        render(buf, ((Ruleset)node).selectors());
        break;

      case SELECTOR:
        append(buf, reprLines(node, 1), " ", true);
        break;
        
      case SELECTORS:
//        position(buf, node);
        buf.indent();
        append(buf, reprLines(node, 3), "\n", true);
        buf.append(" {\n");
        break;

      default:
        buf.indent();
        append(buf, reprLines(node, 1));
        buf.append('\n');
        break;
    }
  }
  
  private static int position(Buffer buf, Node node) {
    String pos = Integer.toString(node.lineOffset());
    int width = POS_COLUMN_WIDTH - pos.length() - 2;
    for (int i = 0; i < width; i++) {
      buf.append(' ');
    }
    buf.append(pos).append("  "); 
    return pos.length();
  }
  
  private static void append(Buffer buf, List<String> lines) {
    append(buf, lines, "\n", true);
  }
  
  private static void append(Buffer buf, List<String> lines, String delim, boolean indent) {
    int size = lines.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(delim);
        if (indent) {
          buf.indent();
        }
      }
      buf.append(lines.get(i));
    }
  }
  
  private static List<String> reprLines(Node node) {
    return reprLines(node, -1);
  }
  
  private static List<String> reprLines(Node node, int limit) {
    Buffer buf = new Buffer(0);
    node.repr(buf);
    return lines(buf.toString(), limit);
  }
  
  private static List<String> lines(String raw, int limit) {
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
