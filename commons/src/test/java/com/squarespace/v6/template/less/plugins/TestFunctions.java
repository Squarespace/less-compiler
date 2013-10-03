package com.squarespace.v6.template.less.plugins;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Node;


public class TestFunctions implements Registry<Function> {

  public static final Function DUMMY3 = new Function("dummy3", "nnn") {
    
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double n0 = number(args.get(0));
      double n1 = number(args.get(1));
      double n2 = number(args.get(2));
      return new Dimension(n0 + n1 + n2);
    }
    
  };
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

}
