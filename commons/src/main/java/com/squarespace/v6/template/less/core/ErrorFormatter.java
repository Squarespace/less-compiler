package com.squarespace.v6.template.less.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import com.squarespace.v6.template.less.ErrorInfo;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Import;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinCallArgs;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Selectors;


public class ErrorFormatter {

  private ErrorInfo primaryError;
  
  private Node primaryNode;
  
  private Deque<Node> context;

  private Buffer buf;

  private int frameWindow;
  
  private int posColWidth;
  
  private Path mainPath;
  
  private Path currentPath;
  
  public ErrorFormatter(Path mainPath, LessException exc, int indent, int frameWindow) {
    this.mainPath = mainPath;
    this.primaryError = exc.primaryError();
    this.primaryNode = exc.primaryNode();
    this.context = exc.errorContext();
    this.buf = new Buffer(indent);
    this.frameWindow = frameWindow;

    // Calculate the width of the line number column
    int pos = (context != null && !context.isEmpty()) ? context.getLast().lineOffset() : primaryNode.lineOffset();
    posColWidth = Integer.toString(pos + 2).length();
    posColWidth = posColWidth < 6 ? 6 : posColWidth;
  }

  public String format() {
    buf.reset();
    buf.append("An error occurred in '" + mainPath + "':\n\n");
    buf.append("Line  Statement\n");
    buf.append("---------------\n");
    formatStack(context);
    buf.append(primaryError.getMessage());
    return buf.toString();
  }

  private void formatStack(Deque<Node> nodes) {
    int size = nodes.size();
    Iterator<Node> iter = nodes.iterator();
    if (size <= (frameWindow * 2)) {
      while (iter.hasNext()) {
        format(iter.next());
      }
      buf.append('\n');
      return;
    }

    // We need to skip some frames..
    int i = 0;
    int limit = size - frameWindow - 1;
    int skipped = 0;
    while (i < size) {
      Node node = iter.next();
      // Emit a frame if we're within the window, or executing an @import
      if (i < frameWindow || i > limit || node.is(NodeType.IMPORT)) {
        if (skipped > 0) {
          formatSkipped(buf, skipped);
          skipped = 0;
        }
        format(node);

      } else {
        skipped++;
      }
      i++;
    }
    buf.append('\n');
  }

  private void format(Node node) {
    position(node);
    render(node);
    buf.incrIndent();
    if (node.is(NodeType.IMPORT)) {
      currentPath = ((Import)node).fileName();
      buf.append("\n.. in '").append(currentPath.toString()).append("':\n");
    }
  }

  private static void formatSkipped(Buffer buf, int skipped) {
    buf.append("\n.. skipped ").append(skipped).append(" frames\n\n");
  }
  
  private int position(Node node) {
    String pos = Integer.toString(node.lineOffset() + 1);
    int width = posColWidth - pos.length() - 2;
    for (int i = 0; i < width; i++) {
      buf.append(' ');
    }
    buf.append(pos).append("  "); 
    return pos.length();
  }

  private void render(Node node) {
    switch (node.type()) {

      case FEATURES:
        buf.append(' ');
        append(reprLines(node), "\n", false);
        break;
        
      case IMPORT:
        buf.indent();
        ((Import)node).repr(buf);
        break;
        
      case MEDIA:
        buf.indent().append("@media");
        render(((Media)node).features());
        buf.append(" {\n");
        break;
      
      case MIXIN_CALL:
        MixinCall call = (MixinCall)node;
        MixinCallArgs args = call.args();
        Selectors selectors = new Selectors(Arrays.asList(call.selector()));
        buf.indent();
        append(reprLines(selectors, 1), " ", false);
        if (args != null) {
          args.repr(buf);
        }
        buf.append(";\n");
        break;
        
      case RULESET:
        render(((Ruleset)node).selectors());
        break;

      case SELECTOR:
        append(reprLines(node, 1), " ", true);
        break;
        
      case SELECTORS:
        buf.indent();
        append(reprLines(node, 3), "\n", true);
        buf.append(" {\n");
        break;

      default:
        buf.indent();
        append(reprLines(node, 1));
        buf.append('\n');
        break;
    }
  }
  
  private void indent() {
    for (int i = 0; i < posColWidth; i++) {
      buf.append(' ');
    }
    buf.indent();
  }
  
  private void append(List<String> lines) {
    append(lines, "\n", true);
  }
  
  private void append(List<String> lines, String delim, boolean indent) {
    int size = lines.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(delim);
        if (indent) {
          indent();
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

