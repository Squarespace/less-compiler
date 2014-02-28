package com.squarespace.v6.template.less.plugins;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.HSLColor;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.RGBColor;
import com.squarespace.v6.template.less.model.Unit;


public class ColorHSLFunctions implements Registry<Function> {

  public static final Function HSL = new Function("hsl", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double lightness = percent(args.get(2));
      return new HSLColor(hue % 360 / 360.0, saturation, lightness, 1.0);
    }
  };

  public static final Function HSLA = new Function("hsla", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double lightness = percent(args.get(2));
      double alpha = percent(args.get(3));
      return new HSLColor(hue % 360 / 360.0, saturation, lightness, alpha);
    }
  };

  public static final Function HSV = new Function("hsv", "ppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double value = percent(args.get(2));
      return RGBColor.fromHSVA(hue % 360 / 360.0, saturation, value, 1.0);
    }
  };

  public static final Function HSVA = new Function("hsva", "pppp") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      double hue = percent(args.get(0));
      double saturation = percent(args.get(1));
      double value = percent(args.get(2));
      double alpha = percent(args.get(3));
      return RGBColor.fromHSVA(hue % 360 / 360.0, saturation, value, alpha);
    }
  };
  
  public static final Function HUE = new Function("hue", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(hsl(args.get(0)).hue());
    }
  };

  public static final Function SATURATION = new Function("saturation", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(hsl(args.get(0)).saturation() * 100.0), Unit.PERCENTAGE);
    }
  };

  public static final Function LIGHTNESS = new Function("lightness", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(hsl(args.get(0)).lightness() * 100.0), Unit.PERCENTAGE);
    }
  };

  public static final Function LUMA = new Function("luma", "c") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      return new Dimension(Math.round(rgb(args.get(0)).luma() * 100.0), Unit.PERCENTAGE);
    }
  };
  
  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }

}
