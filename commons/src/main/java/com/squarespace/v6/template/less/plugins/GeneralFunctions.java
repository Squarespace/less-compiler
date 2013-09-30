package com.squarespace.v6.template.less.plugins;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.EncodeUtils;
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
      str = new Quoted(str.delimiter(), true, str.parts());
      return new Anonymous(env.context().render(str));
    }
  };
  
  public static final Function ESCAPE = new Function("escape", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted str = (Quoted)args.get(0);
      str = str.copy();
      str.setEscape(true);
      String value = env.context().render(str);
      return new Anonymous(EncodeUtils.escape(value));
    }
  };

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
