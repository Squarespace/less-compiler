package com.squarespace.less.exec;

import java.nio.file.Path;

import com.squarespace.less.Context;
import com.squarespace.less.LessException;
import com.squarespace.less.Options;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Features;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.ImportMarker;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinMarker;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Selector;
import com.squarespace.less.model.Selectors;
import com.squarespace.less.model.Stylesheet;


/**
 * Given an executed tree, renders the final output.
 */
public class LessRenderer {

  private final Context ctx;

  private final RenderEnv env;

  private final Options opts;

  private final CssModel model;

  private int traceId;

  private int warningId;

  private LessRenderer(Context context) {
    this.ctx = context;
    this.env = context.newRenderEnv();
    this.opts = context.options();
    this.model = new CssModel(context);
  }

  public static String render(Context context, Stylesheet sheet) throws LessException {
    return new LessRenderer(context).render(sheet);
  }

  private String render(Stylesheet sheet) throws LessException {
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
          throw new LessInternalException("Unhandled node: " + node.type());
      }
    }
  }

  private void renderDefinition(Definition def) throws LessException {
    String warnings = def.warnings();
    if (warnings != null) {
      String repr = "definition '" + def.name() + "'";
      emitWarnings(repr, def.warnings());
    }
    if (opts.tracing()) {
      Path fileName = def.fileName();
      Buffer buf = ctx.acquireBuffer();

      buf.append("  define   ");
      buf.append(def.repr().trim());
      if (fileName != null) {
        buf.append("    ").append(def.fileName().toString());
      }
      buf.append(':').append(def.lineOffset() + 1).append(' ');
      emitTrace(buf.toString());
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
      emitTrace("    start   " + repr + "    " + line + " ");
    } else {
      emitTrace("      end   " + repr + "    " + line + " ");
    }
  }

  private void renderMixinMarker(MixinMarker marker) throws LessException {
    MixinCall call = marker.mixinCall();
    String repr = call.repr().trim();
    Path fileName = call.fileName();
    String line = (fileName != null ? fileName.toString() : "") + ":" + (call.lineOffset() + 1);
    if (marker.beginning()) {
      emitTrace("    start   " + repr + "    " + line + " ");
    } else {
      emitTrace("      end   " + repr + "    " + line + " ");
    }
  }

  /**
   * Render a rule, consisting of a property, value and optional "!important" modifier.
   */
  private void renderRule(Rule rule) throws LessException {
    emitWarnings("next rule", rule.warnings());
    if (opts.tracing()) {
      Path fileName = rule.fileName();
      String line = (fileName != null ? fileName.toString() : "") + ":" + (rule.lineOffset() + 1);
      emitTrace("next rule defined at '" + line + "'");
    }
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

  private void emitTrace(String what) {
    model.comment("/* TRACE[" + (++traceId) + "]: " + what + " */\n");
  }

  private void emitWarnings(String what, String warnings) {
    if (warnings != null) {
      // Build a comment containing all of the warnings.
      model.comment("/* WARNING[" + (++warningId) + "] raised evaluating " + what + ": " + warnings + " */\n");
    }
  }

}
