package com.squarespace.v6.template.less.plugins;

import static com.squarespace.v6.template.less.core.ExecuteErrorMaker.formatFunctionArgs;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.CharClass;
import com.squarespace.v6.template.less.core.EncodeUtils;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Anonymous;
import com.squarespace.v6.template.less.model.BaseColor;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Quoted;
import com.squarespace.v6.template.less.model.RGBColor;


public class GeneralFunctions implements Registry<Function> {
  
  private static String asString(ExecEnv env, Node node, boolean escape) throws LessException {
    if (escape && node.is(NodeType.QUOTED)) { 
      Quoted str = (Quoted)node;
      str = str.copy();
      str.setEscape(true);
      node = str;
    }
    return env.context().render(node);
  }
  
  public static final Function E = new Function("e", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted str = (Quoted)args.get(0);
      str = new Quoted(str.delimiter(), true, str.parts());
      return new Anonymous(env.context().render(str));
    }
  };
  
  public static final Function ESCAPE = new Function("escape", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      String value = asString(env, args.get(0), true);
      return new Anonymous(EncodeUtils.escape(value));
    }
  };

  /**
   * See http://lesscss.org/#reference  "% format" section.
   */
  public static final Function FORMAT = new Function("%", "s.") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted orig = (Quoted)args.get(0);
      String format = asString(env, orig, true);
      
      Buffer buf = env.context().newBuffer();
      int size = format.length();
      int i = 0; // character index
      int j = 1; // argument index
      int formatters = 0;
      boolean error = false;
      while (i < size) {
        char ch = format.charAt(i);
        if (ch != '%') {
          buf.append(ch);
          i++;
          continue;
        }
        
        i++;
        if (i == size) {
          buf.append('%');
          break;
        }
        ch = format.charAt(i);
        if (ch == '%') {
          buf.append('%');
          i++;
          continue;
        }
        formatters++;
        if (j >= args.size()) {
          i++;
          error = true;
          continue;
        }

        Node arg = args.get(j);
        if (arg.is(NodeType.COLOR)) {
          // Force representation of this color to always be hex, not keyword.
          RGBColor color = ((BaseColor)arg).toRGB().copy();
          color.forceHex(true);
          arg = color;
        }
        boolean escape = (ch == 's' || ch == 'S');
        String value = asString(env, arg, escape);
        if (CharClass.uppercase(ch)) {
          value = EncodeUtils.encodeURIComponent(value);
        }
        buf.append(value);
        i++;
        j++;
      }
      if (error) {
        throw new LessException(formatFunctionArgs(formatters, args.size() - 1));
      }
      Quoted result = new Quoted(orig.delimiter(), orig.escaped());
      result.append(new Anonymous(buf.toString()));
      return result;
    }
  };

  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }
  
}
