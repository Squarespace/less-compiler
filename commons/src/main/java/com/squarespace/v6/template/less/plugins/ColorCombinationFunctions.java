package com.squarespace.v6.template.less.plugins;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.exec.Registry;
import com.squarespace.v6.template.less.exec.SymbolTable;
import com.squarespace.v6.template.less.model.Colors;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.RGBColor;


public class ColorCombinationFunctions implements Registry<Function> {

  public static final Function AVERAGE = new Function("average", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = (c1.red() + c2.red()) / 2.0;
      double green = (c1.green() + c2.green()) / 2.0;
      double blue = (c1.blue() + c2.blue()) / 2.0;
      return new RGBColor(red, green, blue);
    }
  };
  
  public static final Function DIFFERENCE = new Function("difference", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = Math.abs(c1.red() - c2.red());
      double green = Math.abs(c1.green() - c2.green());
      double blue = Math.abs(c1.blue() - c2.blue());
      return new RGBColor(red, green, blue);
    }
  };
  
  public static final Function EXCLUSION = new Function("exclusion", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = exclusion(c1.red(), c2.red());
      double green = exclusion(c1.green(), c2.green());
      double blue = exclusion(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };
  
  private static double exclusion(double c1, double c2) {
    return (c1 + c2 * (255 - c1 - c1) / 255.0);
  }
  
  public static final Function HARDLIGHT = new Function("hardlight", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = hardlight(c1.red(), c2.red());
      double green = hardlight(c1.green(), c2.green());
      double blue = hardlight(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };
  
  private static double hardlight(double c1, double c2) {
    if (c2 < 128) {
      return (2 * c2 * c1 / 255.0);
    }
    return (255 - 2 * (255 - c2) * (255 - c1) / 255.0);
  }
  
  public static final Function MIX = new Function("mix", "cc:d") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double weight = 0.5;
      if (args.size() == 3) {
        Dimension dim = (Dimension)args.get(2);
        weight = dim.value() / 100.0;
      }
      return mix(c1, c2, weight);
    }
  };
  
  private static final RGBColor mix(RGBColor c1, RGBColor c2, double weight) {
    double p = weight;
    double w = p * 2 - 1;
    double a = c1.toHSL().alpha() - c2.toHSL().alpha();
    double w1 = (((w * a == -1) ? w : (w + a) / (1 + w * a)) + 1) / 2.0;
    double w2 = 1 - w1;
    double red = c1.red() * w1 + c2.red() * w2;
    double green = c1.green() * w1 + c2.green() * w2;
    double blue = c1.blue() * w1 + c2.blue() * w2;
    double alpha = c1.alpha() * p + c2.alpha() * (1 - p);
    return new RGBColor(red, green, blue, alpha);
  }

  public static final Function MULTIPLY = new Function("multiply", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = (c1.red() * c2.red() / 255.0);
      double green = (c1.green() * c2.green() / 255.0);
      double blue = (c1.blue() * c2.blue() / 255.0);
      return new RGBColor(red, green, blue);
    }
  };
  
  public static final Function NEGATION = new Function("negation", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = negation(c1.red(), c2.red());
      double green = negation(c1.green(), c2.green());
      double blue = negation(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  private static final double negation(double c1, double c2) {
    return 255 - Math.abs(255 - c2 - c1);
  }
  
  public static final Function OVERLAY = new Function("overlay", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = overlay(c1.red(), c2.red());
      double green = overlay(c1.green(), c2.green());
      double blue = overlay(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  private static double overlay(double c1, double c2) {
    if (c1 < 128) {
      return 2 * c1 * c2 / 255.0;
    }
    return 255 - 2 * (255 - c1) * (255 - c2) / 255.0;
  }
  
  public static final Function SCREEN = new Function("screen", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = screen(c1.red(), c2.red());
      double green = screen(c1.green(), c2.green());
      double blue = screen(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };
  
  private static double screen(double c1, double c2) {
    return 255 - (255 - c1) * (255 - c2) / 255.0;
  }
  
  public static final Function SHADE = new Function("shade", "cd") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      Dimension dim = (Dimension)args.get(1);
      return mix(Colors.BLACK, c1, dim.value() / 100.0);
    }
  };

  public static final Function SOFTLIGHT = new Function("softlight", "cc") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      RGBColor c2 = rgb(args.get(1));
      double red = softlight(c1.red(), c2.red());
      double green = softlight(c1.green(), c2.green());
      double blue = softlight(c1.blue(), c2.blue());
      return new RGBColor(red, green, blue);
    }
  };

  private static double softlight(double c1, double c2) {
    double t = c2 * c1 / 255.0;
    double r = t + c1 * (255.0 - (255.0 - c1) * (255.0 - c2) / 255.0 - t) / 255.0;
    return r;
  }
  
  public static final Function TINT = new Function("tint", "cd") {
    @Override
    public Node invoke(ExecEnv env, List<Node> args) throws LessException {
      RGBColor c1 = rgb(args.get(0));
      Dimension dim = (Dimension)args.get(1);
      double weight = dim.value() / 100.0;
      return mix(Colors.WHITE, c1, weight);
    }
  };

  @Override
  public void registerTo(SymbolTable<Function> table) {
    // NO-OP
  }
  
}
