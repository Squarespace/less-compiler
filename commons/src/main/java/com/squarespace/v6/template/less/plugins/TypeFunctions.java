package com.squarespace.v6.template.less.plugins;

import static com.squarespace.v6.template.less.core.Constants.FALSE;
import static com.squarespace.v6.template.less.core.Constants.TRUE;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.Constants;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Unit;


public class TypeFunctions implements Registry<Function> {

  public static final Function ISCOLOR = new Function("iscolor", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return args.get(0).is(NodeType.COLOR) ? TRUE : FALSE;
    }
  };

  public static final Function ISKEYWORD = new Function("iskeyword", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      switch (args.get(0).type()) {
        case FALSE:
        case KEYWORD:
        case TRUE:
          return Constants.TRUE;
        default:
          return Constants.FALSE;
      }
    }
  };

  public static final Function ISNUMBER = new Function("isnumber", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return args.get(0).is(NodeType.DIMENSION) ? TRUE : FALSE;
    }
  };

  public static final Function ISSTRING = new Function("isstring", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return args.get(0).is(NodeType.QUOTED) ? TRUE : FALSE;
    }
  };

  public static final Function ISURL = new Function("isurl", "*") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return args.get(0).is(NodeType.URL) ? TRUE : FALSE;
    }
  };

  private static class DimensionUnitFunction extends Function {
    
    private Unit unit;

    public DimensionUnitFunction(String name, Unit unit) {
      super(name, "*");
      this.unit = unit;
    }
    
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Node arg = args.get(0);
      if (arg.is(NodeType.DIMENSION)) {
        Dimension dim = (Dimension)arg;
        return dim.unit() == unit ? TRUE : FALSE;
      }
      return FALSE;
    }
  }
  
  public static final Function ISEM = new DimensionUnitFunction("isem", Unit.EM);

  public static final Function ISPERCENTAGE = new DimensionUnitFunction("ispercentage", Unit.PERCENTAGE);
  
  public static final Function ISPIXEL = new DimensionUnitFunction("ispixel", Unit.PX);
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

}
