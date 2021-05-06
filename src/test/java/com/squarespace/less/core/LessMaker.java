/**
 * Copyright (c) 2014 SQUARESPACE, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.squarespace.less.core;

import static com.squarespace.less.model.Combinator.DESC;

import java.util.Arrays;

import com.squarespace.less.exec.ArgSpec;
import com.squarespace.less.model.Alpha;
import com.squarespace.less.model.Anonymous;
import com.squarespace.less.model.Argument;
import com.squarespace.less.model.Assignment;
import com.squarespace.less.model.AttributeElement;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Colors;
import com.squarespace.less.model.Combinator;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Condition;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Element;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.ExpressionList;
import com.squarespace.less.model.Feature;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.FunctionCall;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.HSLColor;
import com.squarespace.less.model.Keyword;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Operation;
import com.squarespace.less.model.Operator;
import com.squarespace.less.model.Parameter;
import com.squarespace.less.model.Paren;
import com.squarespace.less.model.Property;
import com.squarespace.less.model.Quoted;
import com.squarespace.less.model.RGBColor;
import com.squarespace.less.model.Ratio;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Shorthand;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.TextElement;
import com.squarespace.less.model.UnicodeRange;
import com.squarespace.less.model.Unit;
import com.squarespace.less.model.Url;
import com.squarespace.less.model.ValueElement;
import com.squarespace.less.model.Variable;


public class LessMaker {

  public Alpha alpha(Node node) {
    return new Alpha(node);
  }

  public Alpha alpha(String raw) {
    return alpha(anon(raw));
  }

  public Anonymous anon() {
    return new Anonymous();
  }

  public Anonymous anon(String raw) {
    return new Anonymous(raw);
  }

  public Argument arg(Node val) {
    return new Argument(val);
  }

  public Argument arg(String name, Node val) {
    return new Argument(name, val);
  }

  public MixinCallArgs args(char delim, Argument ... elems) {
    MixinCallArgs args = new MixinCallArgs(delim);
    for (Argument arg : elems) {
      args.add(arg);
    }
    return args;
  }

  public ArgSpec argspec(String spec) {
    return ArgSpec.fromString(spec);
  }

  public ArgSpec argspec(NodeType ... types) {
    return new ArgSpec(types);
  }

  public ArgSpec argspec(int minArgs, NodeType ... types) {
    return new ArgSpec(minArgs, types);
  }

  public Assignment assign(String name, Node val) {
    return new Assignment(name, val);
  }

  public AttributeElement attr(Combinator comb, Object ... elems) {
    AttributeElement elem = new AttributeElement(comb);
    for (Object part : elems) {
      if (part instanceof String) {
        elem.add(new Anonymous((String)part));

      } else if (part == null || part instanceof Quoted || part instanceof Anonymous) {
        elem.add((Node)part);

      } else {
        throw new IllegalArgumentException("Can't add node of type " +  part.getClass().getSimpleName()
            + " to AttributeElement");
      }
    }
    return elem;
  }

  public Block block(Node ... nodes) {
    Block block = new Block();
    for (Node node : nodes) {
      block.appendNode(node);
    }
    return block;
  }

  public FunctionCall call(String name, Node ... args) {
    return new FunctionCall(name, Arrays.asList(args));
  }

  public RGBColor color(String raw) {
    int[] comp = Colors.nameToRGB(raw);
    if (comp != null) {
      return new RGBColor(comp[0], comp[1], comp[2], true);
    }
    return RGBColor.fromHex(raw);
  }

  public Comment comment(String body, boolean block) {
    return new Comment(body, block);
  }

  public Condition cond(Operator op, Node node0, Node node1) {
    return new Condition(op, node0, node1, false);
  }

  public Condition cond(Operator op, Node node0, Node node1, boolean negate) {
    return new Condition(op, node0, node1, negate);
  }

  public Definition def(String name, Node val) {
    return new Definition(name, val);
  }

  public GenericBlock defs(Definition ... defs) {
    Block block = new Block();
    for (Definition def : defs) {
      block.appendNode(def);
    }
    return new GenericBlock(block);
  }

  public Dimension dim(long value) {
    return new Dimension(value, null);
  }

  public Dimension dim(double value) {
    return new Dimension(value, null);
  }

  public Dimension dim(long value, Unit unit) {
    return new Dimension(value, unit);
  }

  public Dimension dim(double value, Unit unit) {
    return new Dimension(value, unit);
  }

  public Node dir(String name, Node value) {
    return (value instanceof Block) ? new BlockDirective(name, (Block)value) : new Directive(name, value);
  }

  public TextElement element(String name) {
    return element(DESC, name);
  }

  public ValueElement element(Variable ref) {
    return element(null, ref);
  }

  public TextElement element(Combinator combinator, String name) {
    return new TextElement(combinator, name);
  }

  public ValueElement element(Combinator combinator, Variable ref) {
    return new ValueElement(combinator, ref);
  }

  public Expression expn(Node ... nodes) {
    Expression res = new Expression();
    for (Node node : nodes) {
      res.add(node);
    }
    return res;
  }

  public ExpressionList expnlist(Node ... elems) {
    ExpressionList res = new ExpressionList();
    for (Node elem : elems) {
      res.add(elem);
    }
    return res;
  }

  public Feature feature(Node property, Node value) {
    return new Feature(property, value);
  }

  public Features features(Node ... elems) {
    Features features = new Features();
    for (Node elem : elems) {
      features.add(elem);
    }
    return features;
  }

  public Guard guard(Condition ... conditions) {
    Guard guard = new Guard();
    for (Condition cond : conditions) {
      guard.add(cond);
    }
    return guard;
  }

  public HSLColor hsl(double hue, double saturation, double lightness) {
    return hsla(hue, saturation, lightness, 1.0);
  }

  public HSLColor hsla(double hue, double saturation, double lightness, double alpha) {
    return new HSLColor(hue, saturation, lightness, alpha);
  }

  public Keyword kwd(String value) {
    return new Keyword(value);
  }

  public Media media() {
    return new Media();
  }

  public Media media(Features features) {
    return new Media(features);
  }

  public Media media(Features features, Block block) {
    return new Media(features, block);
  }

  public Mixin mixin(String name) {
    return mixin(name, null, null);
  }

  public Mixin mixin(String name, MixinParams params, Guard guard) {
    return new Mixin(name, params, guard);
  }

  public MixinCall mixincall(Selector selector) {
    return mixincall(selector, null, false);
  }

  public MixinCall mixincall(Selector selector, MixinCallArgs args) {
    return mixincall(selector, args, false);
  }

  public MixinCall mixincall(Selector selector, MixinCallArgs args, boolean important) {
    return new MixinCall(selector, args, important);
  }

  public Operation oper(Operator op, Node operand0, Node operand1) {
    return new Operation(op, operand0, operand1);
  }

  public Parameter param(String name) {
    return new Parameter(name);
  }

  public Parameter param(String name, boolean variadic) {
    return new Parameter(name, variadic);
  }

  public Parameter param(String name, Node value) {
    return new Parameter(name, value);
  }

  public MixinParams params(Parameter ... elems) {
    MixinParams params = new MixinParams();
    for (Parameter param : elems) {
      params.add(param);
    }
    return params;
  }

  public Paren paren(Node node) {
    return new Paren(node);
  }

  public Property prop(String name) {
    return new Property(name);
  }

  public Quoted quoted(char delim, boolean escape, Object ... elems) {
    Quoted result = new Quoted(delim, escape);
    for (Object obj : elems) {
      if (obj instanceof Node) {
        result.append((Node)obj);

      } else if (obj instanceof String) {
        result.append(new Anonymous((String)obj));

      } else {
        throw new IllegalArgumentException("Can't add part of type " + obj.getClass().getSimpleName() + " to Quoted");
      }
    }
    return result;
  }

  public Ratio ratio(String value) {
    return new Ratio(value);
  }

  public RGBColor rgb(int red, int green, int blue) {
   return rgb(red, green, blue, 1.0);
  }

  public RGBColor rgb(int red, int green, int blue, double alpha) {
    return rgb(red, green, blue, alpha, false);
  }

  public RGBColor rgb(int red, int green, int blue, double alpha, boolean keyword) {
    return new RGBColor(red, green, blue, alpha, keyword);
  }

  public Rule rule(Node name, Node value) {
    return new Rule(name, value);
  }

  public Rule rule(Node name, Node value, boolean important) {
    return new Rule(name, value, important);
  }

  public Ruleset ruleset(Selector ... selectors) {
    Ruleset res = new Ruleset();
    for (Selector selector : selectors) {
      res.add(selector);
    }
    return res;
  }

  public Selector selector(Element ... elements) {
    Selector result = new Selector();
    for (Element elem : elements) {
      result.add(elem);
    }
    return result;
  }

  public Selectors selectors(Selector ... elems) {
    Selectors selectors = new Selectors();
    for (Selector selector : elems) {
      selectors.add(selector);
    }
    return selectors;
  }

  public Stylesheet stylesheet() {
    return new Stylesheet();
  }

  public Shorthand shorthand(Node left, Node right) {
    return new Shorthand(left, right);
  }

  public UnicodeRange unicode(String raw) {
    return new UnicodeRange(raw);
  }

  public Url url(Node node) {
    return new Url(node);
  }

  public ExpressionList value(Node ... nodes) {
    return new ExpressionList(Arrays.asList(nodes));
  }

  public Variable var(String name) {
    return var(name, false);
  }

  public Variable var(String name, boolean curly) {
    return new Variable(name, curly);
  }

  public ValueElement varelem(Combinator combinator, Variable var) {
    return new ValueElement(combinator, var);
  }

}
