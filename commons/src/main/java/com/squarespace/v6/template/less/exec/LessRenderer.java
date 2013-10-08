package com.squarespace.v6.template.less.exec;

import java.nio.file.Path;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.core.Buffer;
import com.squarespace.v6.template.less.core.FlexList;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.BlockDirective;
import com.squarespace.v6.template.less.model.Comment;
import com.squarespace.v6.template.less.model.Definition;
import com.squarespace.v6.template.less.model.Directive;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.Import;
import com.squarespace.v6.template.less.model.ImportMarker;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinMarker;
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

  private Context ctx;

  private RenderEnv env;

  private Options opts;
  
  private CssModel model;
  
  public LessRenderer() {
  }
  
  public String render(Context context, Stylesheet sheet) throws LessException {
    this.ctx = context;
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
      Buffer buf = ctx.acquireBuffer();
      for (Selector selector : selectors.selectors()) {
        env.render(buf, selector);
        model.header(buf.toString());
        buf.reset();
      }
      ctx.returnBuffer();
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

        case BLOCK_DIRECTIVE:
          renderBlockDirective((BlockDirective)node);
          break;
          
        case COMMENT:
          Comment comment = (Comment)node;
          if (!opts.compress() && comment.block()) {
            model.comment(env.render(comment));
          }
          break;

        case DEFINITION:
          renderDefinition((Definition)node);
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
          
        case IMPORT_MARKER:
          renderImportMarker((ImportMarker)node);
          break;

        case MEDIA:
          renderMedia((Media)node);
          break;

        case MIXIN:
          // Ignore in render phase.
          break;

        case MIXIN_MARKER:
          renderMixinMarker((MixinMarker)node);
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
  
  private void renderDefinition(Definition def) throws LessException {
    if (opts.tracing()) {
      Path fileName = def.fileName();
      Buffer buf = ctx.acquireBuffer();
      buf.append("/* TRACE   define   ");
      buf.append(def.repr().trim());
      if (fileName != null) {
        buf.append("    ").append(def.fileName().toString());
      }
      buf.append(':').append(def.lineOffset() + 1).append("  */\n");
      model.comment(buf.toString());
      ctx.returnBuffer();
    }
  }
  
  private void renderImport(Import imp) throws LessException {
    Buffer buf = new Buffer(0);
    buf.append("@import ");
    env.render(buf, imp.path());
    Features features = imp.features();
    if (features != null && !features.isEmpty()) {
      buf.append(' ');
      env.render(buf, features);
    }
    model.value(buf.toString());
  }
  
  private void renderImportMarker(ImportMarker marker) throws LessException {
    Import imp = marker.importStatement();
    String repr = imp.repr().trim();
    Path fileName = imp.fileName();
    String line = (fileName != null ? fileName.toString() : "") + ":" + (imp.lineOffset() + 1);
    if (marker.beginning()) {
      model.comment("\n/* TRACE    start   " + repr + "    " + line + "  */\n");
    } else {
      model.comment("/* TRACE      end   " + repr + "    " + line + "  */\n");
    }
  }
  
  private void renderMixinMarker(MixinMarker marker) throws LessException {
    MixinCall call = marker.mixinCall();
    String repr = call.repr().trim();
    Path fileName = call.fileName();
    String line = (fileName != null ? fileName.toString() : "") + ":" + (call.lineOffset() + 1);
    if (marker.beginning()) {
      model.comment("/* TRACE    start   " + repr + "    " + line + "  */\n");
    } else {
      model.comment("/* TRACE      end   " + repr + "    " + line + "  */\n");
    }
  }
  
  /**
   * Render a rule, consisting of a property, value and optional "!important" modifier.
   */
  private void renderRule(Rule rule) throws LessException {
    Buffer buf = ctx.acquireBuffer();
    env.render(buf, rule.property());
    buf.ruleSep();
    env.render(buf, rule.value());
    if (rule.important()) {
      buf.append(" !important");
    }
    model.value(buf.toString());
    ctx.returnBuffer();
  }

}
