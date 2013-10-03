package com.squarespace.v6.template.less.exec;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.FlexList;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.BlockDirective;
import com.squarespace.v6.template.less.model.Comment;
import com.squarespace.v6.template.less.model.Directive;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Import;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Rule;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Selector;
import com.squarespace.v6.template.less.model.Selectors;
import com.squarespace.v6.template.less.model.Stylesheet;


/**
 * Given an executed tree, renders the final output.
 */
public class LessRenderer {

  private RenderEnv env;
  
  private Options opts;
  
  private CssModel model;
  
  public LessRenderer() {
  }
  
  public String render(Context context, Stylesheet sheet) throws LessException {
    this.env = context.newRenderEnv();
    this.opts = context.options();
    this.model = new CssModel(context);

    env.push(sheet);
    Block block = sheet.block();
    Directive charset = block.charset();
    if (charset != null) {
      model.value(env.render(charset));
    }
    renderImports(block);
    renderBlock(block, false);
    env.pop();

    return model.render();
  }
  
  private void renderRuleset(Ruleset ruleset) throws LessException {
    env.push(ruleset);
    model.push(NodeType.RULESET);

    Selectors selectors = env.frame().selectors();
    if (!selectors.isEmpty()) {
      // Selectors are indented and delimited by the model.
      Buffer buf = env.context().newBuffer();
      for (Selector selector : selectors.selectors()) {
        env.render(buf, selector);
        model.header(buf.toString());
        buf.reset();
      }
    }
    
    renderBlock(ruleset.block(), true);
    model.pop();
    env.pop();
  }
  
  private void renderMedia(Media media) throws LessException {
    env.push(media);
    model.push(NodeType.MEDIA);
    model.header("@media " + env.render(env.frame().features()));
    
    // Force any parent selectors to be emitted, to wrap our rules.
    Ruleset inner = new Ruleset();
    inner.setBlock(media.block());
    renderRuleset(inner);

    model.pop();
    env.pop();
  }

  private void renderBlockDirective(BlockDirective directive) throws LessException {
    env.push(directive);
    model.push(NodeType.BLOCK_DIRECTIVE);
    model.header(directive.name());
    
    renderBlock(directive.block(), true);

    model.pop();
    env.pop();
  }
  
  private void renderImports(Block block) throws LessException {
    FlexList<Node> rules = block.rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      switch (node.type()) {
        
        case IMPORT:
          renderImport((Import)node);
          break;
          
        default:
          break;
      }
    }
  }
  
  private void renderBlock(Block block, boolean includeImports) throws LessException {
    FlexList<Node> rules = block.rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      switch (node.type()) {

        case DEFINITION:
        case MIXIN:
          // Ignore in render phase.
          break;

        case BLOCK_DIRECTIVE:
          renderBlockDirective((BlockDirective)node);
          break;
          
        case COMMENT:
          Comment comment = (Comment)node;
          if (!opts.compress() && comment.block()) {
            model.comment(env.render(comment));
          }
          break;

        case DIRECTIVE:
          Directive directive = (Directive)node;
          if (!directive.name().equals("@charset")) {
            model.value(env.render(directive));
          }
          break;

        case IMPORT:
          if (includeImports) {
            renderImport((Import)node);
          }
          break;

        case MEDIA:
          renderMedia((Media)node);
          break;

        case RULE:
          renderRule((Rule)node);
          break;

        case RULESET:
          renderRuleset((Ruleset)node);
          break;
          
        default:
          throw new RuntimeException("Unhandled node: " + node.type());
      }
    }
  }
  
  private void renderImport(Import imp) throws LessException {
    String value = "@import " + env.render(imp.path());
    Features features = imp.features();
    if (features != null && !features.isEmpty()) {
      value += " " + env.render(features);
    }
    model.value(value);
  }
  
  /**
   * Render a rule, consisting of a property, value and optional "!important" modifier.
   */
  private void renderRule(Rule rule) throws LessException {
    Buffer buf = env.context().newBuffer();
    env.render(buf, rule.property());
    buf.ruleSep();
    env.render(buf, rule.value());
    if (rule.important()) {
      buf.append(" !important");
    }
    model.value(buf.toString());
  }

}
