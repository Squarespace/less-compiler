package com.squarespace.less.plugins;

import java.util.List;

import com.squarespace.less.LessException;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.exec.Registry;
import com.squarespace.less.exec.SymbolTable;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.RGBColor;


public class ColorRGBFunctions implements Registry<Function> {

  public static final Function ALPHA = new Function("alpha", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.alpha());
    }
  };

  public static final Function ARGB = new Function("argb", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return color.toARGB();
    }
  };
  
  public static final Function BLUE = new Function("blue", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.blue());
    }
  };

  public static final Function COLOR = new Function("color", "s") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      Quoted str = (Quoted)args.get(0);
      str = str.copy();
      str.setEscape(true);
      String repr = env.context().render(str);
      return RGBColor.fromHex(repr);
    }
  };
  
  public static final Function GREEN = new Function("green", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.green());
    }
  };

  public static final Function RED = new Function("red", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor color = rgb(args.get(0));
      return new Dimension(color.red());
    }
  };

  public static final Function RGB = new Function("rgb", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double red = scaled(args.get(0), 256);
      double green = scaled(args.get(1), 256);
      double blue = scaled(args.get(2), 256);
      return new RGBColor(red, green, blue);
    }
  };

  public static final Function RGBA = new Function("rgba", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double red = scaled(args.get(0), 256);
      double green = scaled(args.get(1), 256);
      double blue = scaled(args.get(2), 256);
      double alpha = percent(args.get(3));
      return new RGBColor(red, green, blue, alpha);
    }
  };
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }
  
}
