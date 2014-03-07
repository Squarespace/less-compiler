package com.squarespace.less.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import com.squarespace.less.exec.ReprUtils;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.ParseError;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selectors;


/**
 * Given a stack trace built by a LessException, it filters and formats the representation
 * of the stack to be used in an error message.
 */
public class StackFormatter {

  private final Deque<Node> stack;

  private final Buffer buf;

  private final int indentIncr;

  private final int frameWindow;

  private List<Entry> result;

  private int indentLevel;

  private int col1Width = 0;

  private int col2Width = 0;

  public StackFormatter(Deque<Node> stack, int indent, int frameWindow) {
    this.stack = stack;
    this.buf = new Buffer(0);
    this.indentIncr = indent;
    this.frameWindow = frameWindow;
  }

  public String format() {
    result = new ArrayList<>();
    int size = stack.size();
    Iterator<Node> iter = stack.iterator();
    if (size <= (frameWindow * 2)) {
      while (iter.hasNext()) {
        render(iter.next());
      }
      return format(result, col1Width, col2Width);
    }

    int i = 0;
    int limit = size - frameWindow - 1;
    int skipped = 0;
    while (i < size) {
      Node node = iter.next();
      if (i < frameWindow || i > limit || node.is(NodeType.IMPORT)) {
        if (skipped > 0) {
          result.add(renderSkipped(skipped));
          skipped = 0;
        }
        render(node);
      } else {
        skipped++;
      }
      i++;
    }
    return format(result, col1Width, col2Width);
  }

  private String format(List<Entry> entries, int col1, int col2) {
    buf.reset();
    String head = "Line";
    buf.append(head);
    indent((col1 + col2 + 1) - head.length());
    buf.append("  Statement\n");
    int size = buf.length() - 1;
    for (int i = 0; i < size; i++) {
      buf.append('-');
    }
    buf.append('\n');

    for (Entry entry : entries) {
      if (entry.indent) {
        indent(entry.fileName == null ? col1 : col1 - entry.fileName.length());
        if (entry.fileName != null) {
          buf.append(entry.fileName);
        }
        if (entry.lineNo != null) {
          buf.append(':').append(entry.lineNo);
        } else {
          buf.append(' ');
        }
        indent(entry.lineNo == null ? col2 : col2 - entry.lineNo.length());
        buf.append("  ");
      }
      buf.append(entry.repr).append('\n');
    }
    return buf.toString();
  }

  private void indent() {
    for (int i = 0; i < indentLevel; i++) {
      indent(indentIncr);
    }
  }

  private void indent(int size) {
    for (int i = 0; i < size; i++) {
      buf.append(' ');
    }
  }

  private void render(Node node) {
    if (node.is(NodeType.PARSE_ERROR)) {
      ParseError error = (ParseError)node;
      Path filePath = error.filePath();
      if (filePath != null) {
        Entry entry = new Entry(null, null, "In '" + filePath + "':");
        entry.indent = false;
        result.add(entry);
      }
      Entry entry = new Entry(null, null, error.errorMessage());
      entry.indent = false;
      result.add(entry);
      return;
    }

    Entry entry = renderEntry(node);
    if (entry.fileName != null) {
      col1Width = Math.max(col1Width, entry.fileName.length());
    }
    if (entry.lineNo != null) {
      col2Width = Math.max(col2Width, entry.lineNo.length());
    }
    result.add(entry);
    indentLevel++;
  }

  private Entry renderEntry(Node node) {
    switch (node.type()) {

      case BLOCK_DIRECTIVE:
        BlockDirective blockDir = (BlockDirective)node;
        buf.reset();
        indent();
        buf.append(blockDir.name()).append(" {");
        return new Entry(blockDir.fileName(), blockDir.lineOffset() + 1, buf.toString());

      case DEFINITION:
        Definition def = (Definition)node;
        buf.reset();
        indent();
        def.repr(buf);
        return new Entry(def.fileName(), def.lineOffset() + 1, buf.toString());

      case DIRECTIVE:
        Directive directive = (Directive)node;
        buf.reset();
        indent();
        directive.repr(buf);
        return new Entry(directive.fileName(), directive.lineOffset() + 1, buf.toString());

      case IMPORT:
        Import imp = (Import)node;
        buf.reset();
        indent();
        imp.repr(buf);
        return new Entry(imp.fileName(), imp.lineOffset() + 1, buf.toString());

      case MEDIA:
        Media media = (Media)node;
        Features features = media.features();
        buf.reset();
        indent();
        buf.append("@media");
        if (features != null) {
          buf.append(' ');
          features.repr(buf);
        }
        buf.append(" {");
        return new Entry(media.fileName(), media.lineOffset() + 1, buf.toString());

      case MIXIN_CALL:
        MixinCall call = (MixinCall)node;
        MixinCallArgs args = call.args();
        Selectors selectors = new Selectors(Arrays.asList(call.selector()));
        buf.reset();
        indent();
        selectors.repr(buf);
        if (args != null) {
          args.repr(buf);
        }
        buf.append(';');
        return new Entry(call.fileName(), call.lineOffset() + 1, buf.toString());

      case RULE:
        Rule rule = (Rule)node;
        buf.reset();
        indent();
        rule.repr(buf);
        return new Entry(rule.fileName(), rule.lineOffset() + 1, buf.toString());

      case RULESET:
        Ruleset ruleset = (Ruleset)node;
        buf.reset();
        indent();
        append(ReprUtils.reprLines(ruleset.selectors(), 3), " ");
        buf.append(" {");
        return new Entry(ruleset.fileName(), ruleset.lineOffset() + 1, buf.toString());

      default:
        buf.reset();
        indent();
        node.repr(buf);
        return new Entry(null, node.lineOffset() + 1, buf.toString());
    }
  }

  private void append(List<String> lines, String delim) {
    int size = lines.size();
    for (int i = 0; i < size; i++) {
      if (i > 0) {
        buf.append(delim);
      }
      buf.append(lines.get(i));
    }
  }

  private Entry renderSkipped(int skipped) {
    return new Entry(null, null, "\n.. skipped " + skipped + " frames\n");
  }

  private static class Entry {

    public final String fileName;

    public final String lineNo;

    public final String repr;

    public boolean indent;

    public Entry(Path fileName, int lineNo, String repr) {
      this(fileName == null ? null : fileName.toString(), Integer.toString(lineNo), repr);
    }

    public Entry(String fileName, String lineNo, String repr) {
      this.fileName =fileName;
      this.lineNo = lineNo;
      this.repr = repr;
      this.indent = true;
    }
  }

}
