package com.squarespace.less.plugins;

import static com.squarespace.less.core.ExecuteErrorMaker.unknownUnit;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.exec.SymbolTable;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.Unit;


public class NumericFunctions implements Registry<Function> {

  public static final Function CEIL = new Function("ceil", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.ceil(dim.value()), dim.unit());
    }
  };

  public static final Function FLOOR = new Function("floor", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(Math.floor(dim.value()), dim.unit());
    }
  };

  public static final Function PERCENTAGE = new Function("percentage", "d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      return new Dimension(dim.value() * 100, Unit.PERCENTAGE);
    }
  };

  public static final Function ROUND = new Function("round", "d:n") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double places = 0.0;
      int size = args.size();
      Dimension dim = (Dimension)args.get(0);
      if (size == 2) {
        places = Math.max(((Dimension)args.get(1)).value(), 0);
      }
      double scale = Math.pow(10, places);
      return new Dimension(Math.round(dim.value() * scale) / scale, dim.unit());
    }
  };

  public static final Function UNIT = new Function("unit", "d:*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Dimension dim = (Dimension)args.get(0);
      Unit unit = null;
      if (args.size() >= 2) {
        Node node = (Node)args.get(1);
        if (node.is(NodeType.KEYWORD)) {
          Keyword word = (Keyword)node;
          unit = Unit.get(word.value());
          
        } else if (node.is(NodeType.QUOTED)) {
          Quoted quoted = (Quoted)node;
          quoted = new Quoted(quoted.delimiter(), true, quoted.parts());
          String repr = env.context().render(quoted);
          unit = Unit.get(repr);

        }
        if (unit == null) {
          throw new LessException(unknownUnit(node.repr()));
        }
      }
      return new Dimension(dim.value(), unit);
    }
  };
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
   // NO-OP
  }

}
