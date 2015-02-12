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

package com.squarespace.less.exec;

import java.nio.file.Path;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.Buffer;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Comment;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.DetachedRuleset;
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
 * Given an executed tree, renders the final CSS output.
 */
public class LessRenderer {

  /**
   * Context for the current compile.
   */
  private final LessContext ctx;

  /**
   * {@link Stylesheet} instance to render.
   */
  private final Stylesheet stylesheet;

  /**
   * Rendering enviromment.
   */
  private final RenderEnv env;

  /**
   * Options for the current compile.
   */
  private final LessOptions opts;

  /**
   * CSS model to build.
   */
  private final CssModel model;

  /**
   * Sequence for generating trace identifiers.
   */
  private int traceId;

  /**
   * Sequence for generating warning identifiers.
   */
  private int warningId;

  protected LessRenderer(LessContext context, Stylesheet stylesheet) {
    this.ctx = context;
    this.stylesheet = stylesheet;
    this.env = context.newRenderEnv();
    this.opts = context.options();
    this.model = new CssModel(context);
  }

  /**
   * Shortcut to render a stylesheet against the given context.
   */
  public static String render(LessContext context, Stylesheet sheet) throws LessException {
    return new LessRenderer(context, sheet).render();
  }

  /**
   * Render the {@link Stylesheet} to the {@link CssModel} and return the
   * rendered output.
   */
  public String render() throws LessException {
    env.push(stylesheet);
    Block block = stylesheet.block();
    Directive charset = block.charset();
    if (charset != null) {
      model.value(ctx.render(charset));
    }
    renderImports(block);
    renderBlock(block, false);
    env.pop();

    return model.render();
  }

  /**
   * Render a {@link Ruleset}
   */
  private void renderRuleset(Ruleset ruleset) throws LessException {
    env.push(ruleset);
    model.push(NodeType.RULESET);

    Selectors selectors = env.frame().selectors();
    if (!selectors.isEmpty()) {
      // Selectors are indented and delimited by the model.
      Buffer buf = ctx.acquireBuffer();
      for (Selector selector : selectors.selectors()) {
        ctx.render(buf, selector);
        model.header(buf.toString());
        buf.reset();
      }
      ctx.returnBuffer();
    }

    renderBlock(ruleset.block(), true);
    model.pop();
    env.pop();
  }

  /**
   * Render a {@link Media}
   */
  private void renderMedia(Media media) throws LessException {
    env.push(media);
    model.push(NodeType.MEDIA);
    model.header("@media " + ctx.render(env.frame().features()));

    // Force any parent selectors to be emitted, to wrap our rules.
    Ruleset inner = new Ruleset();
    inner.setBlock(media.block());
    renderRuleset(inner);

    model.pop();
    env.pop();
  }

  /**
   * Render a {@link BlockDirective}
   */
  private void renderBlockDirective(BlockDirective directive) throws LessException {
    env.push(directive);
    model.push(NodeType.BLOCK_DIRECTIVE);
    model.header(directive.name());

    renderBlock(directive.block(), true);

    model.pop();
    env.pop();
  }

  /**
   * Render all {@Import} rules found in the given block.
   */
  private void renderImports(Block block) throws LessException {
    if (!block.hasImports()) {
      return;
    }
    FlexList<Node> rules = block.rules();
    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      if (node instanceof Import) {
        renderImport((Import)node);
      }
    }
  }

  /**
   * Render all children for the given {@link Block}, optionally including
   * imports.
   */
  private void renderBlock(Block block, boolean includeImports) throws LessException {
    LessBlockRuleMerger ruleMerger = block.hasPropertyMergeModes() ? new LessBlockRuleMerger(ctx) : null;
    FlexList<Node> rules = block.rules();

    int size = rules.size();
    for (int i = 0; i < size; i++) {
      Node node = rules.get(i);
      switch (node.type()) {

        case BLOCK:
          renderBlock((Block)node, includeImports);
          break;

        case BLOCK_DIRECTIVE:
          renderBlockDirective((BlockDirective)node);
          break;

        case COMMENT:
          Comment comment = (Comment)node;
          if (comment.block() && (!opts.compress() || comment.hasBang())) {
            model.comment(ctx.render(comment));
          }
          break;

        case DEFINITION:
          renderDefinition((Definition)node);
          break;

        case DETACHED_RULESET:
        {
          DetachedRuleset ruleset = (DetachedRuleset)node;
          renderBlock(ruleset.block(), includeImports);
          break;
        }

        case DIRECTIVE:
        {
          Directive directive = (Directive)node;
          if (!directive.name().equals("@charset")) {
            model.value(ctx.render(directive));
          }
          break;
        }

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
          if (ruleMerger == null) {
            renderRule((Rule)node);
          } else {
            ruleMerger.add((Rule)node);
          }
          break;

        case RULESET:
          renderRuleset((Ruleset)node);
          break;

        default:
          throw new LessInternalException("Unhandled node: " + node.type());
      }
    }

    // If rule merging was in effect, we need to render all rules here.
    if (ruleMerger != null) {
      for (Rule rule : ruleMerger.rules()) {
        renderRule(rule);
      }
    }
  }

  /**
   * Render a {@link Definition}.
   */
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

  /**
   * Render an {@link Import}
   */
  private void renderImport(Import imp) throws LessException {
    Buffer buf = new Buffer(0);
    buf.append("@import ");
    ctx.render(buf, imp.path());
    Features features = imp.features();
    if (features != null && !features.isEmpty()) {
      buf.append(' ');
      ctx.render(buf, features);
    }
    model.value(buf.toString());
  }

  /**
   * Render an {@link ImportMarker}
   */
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

  /**
   * Render a {@link MixinMarker}
   */
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
    ctx.render(buf, rule.property());
    buf.ruleSep();
    ctx.render(buf, rule.value());
    if (rule.important()) {
      buf.append(" !important");
    }
    model.value(buf.toString());
    ctx.returnBuffer();
  }

  /**
   * Emit a tracing comment.
   */
  private void emitTrace(String what) {
    model.comment("/* TRACE[" + (++traceId) + "]: " + what + " */\n");
  }

  /**
   * Emit a warning comment.
   */
  private void emitWarnings(String what, String warnings) {
    if (warnings != null) {
      // Build a comment containing all of the warnings.
      model.comment("/* WARNING[" + (++warningId) + "] raised evaluating " + what + ": " + warnings + " */\n");
    }
  }

}
