package com.squarespace.v6.template.less.exec;

import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.BaseColor;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.HSLColor;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.RGBColor;
import com.squarespace.v6.template.less.model.Unit;


public abstract class Function {

  protected String name;
  
  protected ArgSpec spec;
  
  public Function(String name, String spec) {
    this.name = name;
    this.spec = ArgSpec.parseSpec(spec);
  }
  
  public Function(String name, ArgSpec spec) {
    this.name = name;
    this.spec = spec;
  }
  
  public String name() {
    return name;
  }

  public ArgSpec spec() {
    return spec;
  }
  
  public abstract Node invoke(ExecEnv env, List<Node> args) throws LessException;
  
  public static double number(Node node) throws LessException {
    Dimension dim = (Dimension)node;
    if (dim.unit() == Unit.PERCENTAGE) {
      return dim.value() / 100.0;
    }
    return dim.value();
  }

  public static double scaled(Node node, double scale) throws LessException {
    Dimension dim = (Dimension)node;
    double value = number(node);
    return dim.unit() == Unit.PERCENTAGE ? (value * .01) * scale : value;
  }
  
  public static HSLColor hsl(Node node) throws LessException {
    return ((BaseColor)node).toHSL();
  }

  public static RGBColor rgb(Node node) throws LessException {
    return ((BaseColor)node).toRGB();
  }

}
