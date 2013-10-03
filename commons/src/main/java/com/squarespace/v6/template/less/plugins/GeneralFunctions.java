package com.squarespace.v6.template.less.plugins;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Anonymous;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Quoted;


public class GeneralFunctions implements Registry<Function> {
  
  public static final Function E = new Function("e", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted str = (Quoted)args.get(0);
      str = new Quoted('"', true, str.parts());
      return new Anonymous(env.context().render(str));
    }
  };
  
  public static final Function ESCAPE = new Function("escape", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted str = (Quoted)args.get(0);
      // XXX: implement
      return null;
      
    }
  };

  // XXX: this needs to be escapeURI()
  private String escape(String str) {
    StringBuilder buf = new StringBuilder();
    int size = str.length();
    for (int i = 0; i < size; i++) {
      char ch = str.charAt(i);
      switch (ch) {
        case '=': buf.append("%3D"); break;
        case ':': buf.append("%3A"); break;
        case '#': buf.append("%3B"); break;
        case '(': buf.append("%28"); break;
        case ')': buf.append("%29"); break;
        default: buf.append(ch);
      }
    }
    return buf.toString();
  }
  /*
    escape: function (str) {
        return new(tree.Anonymous)(encodeURI(str.value).replace(/=/g, "%3D")
        .replace(/:/g, "%3A").replace(/#/g, "%23").replace(/;/g, "%3B")
        .replace(/\(/g, "%28").replace(/\)/g, "%29"));
   */


  
  public static final Function FORMAT = new Function("%", "s.") {
    
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      // XXX: implement
      return null;
    }
  };

  /*  
  '%': function (quoted, ...) {
    var args = Array.prototype.slice.call(arguments, 1),
        str = quoted.value;

    for (var i = 0; i < args.length; i++) {
        str = str.replace(/%[sda]/i, function(token) {
            var value = token.match(/s/i) ? args[i].value : args[i].toCSS();
            return token.match(/[A-Z]$/) ? encodeURIComponent(value) : value;
        });
    }
    str = str.replace(/%%/g, '%');
    return new(tree.Quoted)('"' + str + '"', str);
  },
  */
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }
  
}
