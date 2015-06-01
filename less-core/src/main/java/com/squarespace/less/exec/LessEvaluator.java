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

import static com.squarespace.less.core.Constants.FALSE;
import static com.squarespace.less.core.ExecuteErrorMaker.mixinRecurse;
import static com.squarespace.less.core.ExecuteErrorMaker.mixinUndefined;
import static com.squarespace.less.core.ExecuteErrorMaker.varBlockLevel;

import java.util.List;

import com.squarespace.less.LessContext;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.core.FlexList;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.BlockDirective;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.DetachedRuleset;
import com.squarespace.less.model.Directive;
import com.squarespace.less.model.Dummy;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.Guard;
import com.squarespace.less.model.Import;
import com.squarespace.less.model.ImportMarker;
import com.squarespace.less.model.Media;
import com.squarespace.less.model.Mixin;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinMarker;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Rule;
import com.squarespace.less.model.Ruleset;
import com.squarespace.less.model.Stylesheet;
import com.squarespace.less.model.True;
import com.squarespace.less.model.Variable;


/**
 * Given a parse tree, evaluate it. This expands all variable definitions,
 * mixins, imports, etc. This produces a tree that is ready to be rendered.
 */
public class LessEvaluator {

  /**
   * Context for the current compile.
   */
  private final LessContext ctx;

  /**
   * Options for the current compile.
   */
  private final LessOptions opts;

  public LessEvaluator(LessContext ctx) {
    this.ctx = ctx;
    this.opts = ctx.options();
  }

  /**
   * Evaluate the {@link Stylesheet}, producing a new instance where all variable references,
   * mixins, imports, etc have been evaluated and expanded.
   */
  public Stylesheet evaluate(Stylesheet sheet) throws LessException {
    ExecEnv env = ctx.newEnv();
    Stylesheet result = evaluateStylesheet(env, sheet);
    if (env.hasError()) {
      throw env.error();
    }
    return result;
  }

  /**
   * Evaluate a BLOCK_DIRECTIVE node.
   */
  private BlockDirective evaluateBlockDirective(ExecEnv env, BlockDirective input) throws LessException {
    BlockDirective directive = input.copy();
    env.push(directive);

    Block block = directive.block();
    expandMixins(env, block);
    evaluateRules(env, block, false);

    env.pop();
    return directive;
  }

  /**
   * Evaluate a MEDIA node.
   */
  private Media evaluateMedia(ExecEnv env, Media input) throws LessException {
    Media media = input.copy(env);
    media.extendIndex(new ExtendIndex());
    env.push(media);

    Block block = media.block();
    expandMixins(env, block);
    evaluateRules(env, block, false);

    env.pop();
    return media;
  }

  /**
   * Evaluate a RULESET node.
   */
  private Ruleset evaluateRuleset(ExecEnv env, Ruleset input, boolean forceImportant) throws LessException {
    Ruleset original = (Ruleset)input.original();
    Ruleset ruleset = input.copy(env);

    env.push(ruleset);
    original.enter();

    Block block = ruleset.block();
    expandMixins(env, block);
    evaluateRules(env, block, forceImportant);

    original.exit();
    env.pop();
    ruleset.evaluated(true);
    return ruleset;
  }

  /**
   * Evaluate a DETACHED_RULESET node.
   */
  public DetachedRuleset evaluateDetachedRuleset(ExecEnv env, DetachedRuleset input, boolean forceImportant)
      throws LessException {

    ExecEnv closure = input.closure();
    if (closure != null) {
      env = env.copy();
      env.append(closure.frames());
    }

    DetachedRuleset ruleset = input.copy(env);

    env.push(ruleset);
    Block block = ruleset.block();
    expandMixins(env, block);
    evaluateRules(env, block, forceImportant);

    if (closure == null) {
      env.pop();
    }
    return ruleset;
  }

  /**
   * Evaluate a STYLESHEET node.
   */
  private Stylesheet evaluateStylesheet(ExecEnv env, Stylesheet original) throws LessException {
    Stylesheet stylesheet = original.copy();
    stylesheet.extendContext(new ExtendIndex());
    env.push(stylesheet);

    Block block = stylesheet.block();
    expandMixins(env, block);
    evaluateRules(env, block, false);

    env.pop();
    return stylesheet;
  }

  /**
   * Iterate over all of the rules in the block and evaluate them, replacing each rule in
   * the list with the result of the evaluation.
   */
  private void evaluateRules(ExecEnv env, Block block, boolean forceImportant) throws LessException {
    FlexList<Node> rules = block.rules();

    Import currentImport = null;
    for (int i = 0; i < rules.size(); i++) {
      Node node = rules.get(i);

      try {
        switch (node.type()) {

          case BLOCK_DIRECTIVE:
            node = evaluateBlockDirective(env, (BlockDirective)node);
            break;

          case DEFINITION:
          {
            Definition definition = (Definition)node;
            Node value = definition.value();

            // Defer evaluation of detached rulesets. They will be evaluated
            // when they are referenced.  Set the closure for the environment
            // in which the detached ruleset is defined.
            if (value instanceof DetachedRuleset) {
              DetachedRuleset ruleset = (DetachedRuleset)value;
              ruleset.closure(env);

            } else {
              // Definitions has a non-BlockNode value.
              Definition newDef = definition.copy(definition.dereference(env));
              newDef.warnings(env.warnings());
              node = newDef;
            }
            break;
          }

          case DIRECTIVE:
          {
            Directive directive = (Directive)(node.eval(env));
            if (directive.name().equals("@charset")) {
              if (block.charset() == null) {
                block.charset(directive);
              }
            }
            node = directive;
            break;
          }

          case IMPORT_MARKER:
          {
            ImportMarker marker = (ImportMarker) node;
            currentImport = marker.beginning() ? marker.importStatement() : null;
            break;
          }

          case MEDIA:
            node = evaluateMedia(env, (Media)node);
            break;

          case MIXIN:
          {
            // Register the closure on the original MIXIN.
            Mixin mixin = (Mixin) ((Mixin)node).original();
            if (mixin.closure() == null) {
              mixin.closure(env);
            }
            break;
          }

          case MIXIN_CALL:
            throw new LessInternalException("Serious error: all mixin calls should already have been evaluated.");

          case RULESET:
          {
            Ruleset ruleset = (Ruleset)node;
            Guard guard = ruleset.selectors().guard();
            if (evaluateGuard(env, guard)) {
              node = evaluateRuleset(env, ruleset, forceImportant);
            } else {
              node = Dummy.fromNode(ruleset);
            }
            break;
          }

          case RULE:
          {
            Rule rule = (Rule) node;
            Rule newRule = null;
            if (forceImportant && !rule.important()) {
              Node value = rule.value().eval(env);
              newRule = rule.copy(value, forceImportant);

            } else {
              newRule = (Rule)rule.eval(env);
            }
            newRule.warnings(env.warnings());
            node = newRule;
            break;
          }

          case VARIABLE:
          {
            Variable var = (Variable)node;
            if (!var.ruleset()) {
              throw new LessException(varBlockLevel(var.name()));
            }
            node = evaluateDetachedRuleset(env, (DetachedRuleset)node.eval(env), forceImportant);
            break;
          }

          default:
            node = node.eval(env);
            break;
        }

      } catch (LessException e) {
        if (!env.hasError()) {
          env.error(e);
        }
      }

      if (env.hasError()) {
        // If an error occurred, capture the current stack and return.
        LessException error = env.error();
        error.push(node);
        if (currentImport != null) {
          // Track when import boundaries are crossed
          error.push(currentImport);
        }
        return;
      }

      rules.set(i, node);
    }
  }

  /**
   * Iterate over all rules in this block and execute all of the MIXIN_CALL rules found.
   * Each successful call will produce multiple rules. We replace the call with
   * the rules it produced.
   */
  private void expandMixins(ExecEnv env, Block block) throws LessException {
    if (!block.hasMixinCalls()) {
      return;
    }
    FlexList<Node> rules = block.rules();
    // Use of rules.size() intentional since the list size can change during iteration.
    for (int i = 0; i < rules.size(); i++) {
      Node node = rules.get(i);
      if (node instanceof MixinCall) {
        Block mixinResult = null;
        try {
          mixinResult = executeMixinCall(env, (MixinCall)node);

        } catch (LessException e) {
          e.push(node);
          throw e;
        }

        // Splice the rules produced by the mixin call into the current block,
        // replacing the mixin call.
        FlexList<Node> other = mixinResult.rules();
        rules.splice(i, 1, other);
        i += other.size() - 1;

        // Indicate the block has changed, new variable definitions may have
        // been added.
        block.resetVariableCache();
        block.orFlags(mixinResult);
      }
    }
  }

  /**
   * Execute a MIXIN_CALL. First it searches the tree for any MIXIN and RULESET nodes that
   * match the call's selector.  If no matches are found, it throws an error.  If matches
   * are found it executes each match.  In order for the call to be considered successful,
   * the call must (a) bind arguments and be called, or (b) bind arguments but evaluate
   * the mixin's guard to FALSE.
   */
  private Block executeMixinCall(ExecEnv env, MixinCall call) throws LessException {
    MixinMatcher matcher = new MixinMatcher(env, call);
    MixinResolver resolver = ctx.mixinResolver();
    resolver.reset(matcher);
    env.resolveMixins(resolver);
    List<MixinMatch> matches = resolver.matches();
    if (matches.isEmpty()) {
      throw new LessException(mixinUndefined(call.repr()));
    }

    Block results = new Block();
    int calls = 0;
    for (MixinMatch match : matches) {
      Node node = match.mixin();
      if (node instanceof Mixin) {
        if (executeMixin(env, results, matcher, match)) {
          calls++;
        }
      } else if (node instanceof Ruleset) {
        if (executeRulesetMixin(env, results, matcher, match)) {
          calls++;
        }
      }
    }

    if (calls == 0) {
      throw new LessException(mixinUndefined(call.repr()));
    }
    return results;
  }

  /**
   * Execute a MIXIN's block.  If argument binding fails, returns false, indicating the
   * call did not successfully match this mixin.  If argument binding succeeds, it sets
   * up the stack and evaluates the mixin's guard expression, if any.
   *
   * If the guard evaluates to FALSE, returns true indicating the mixin was successfully
   * matched but just not executed.
   *
   * If the guard evaluates to TRUE, we execute the mixin's block and merge the produced
   * rules into the 'collector' block.
   */
  private boolean executeMixin(ExecEnv env, Block collector, MixinMatcher matcher, MixinMatch match)
      throws LessException {

    MixinCall call = matcher.mixinCall();
    Mixin mixin = ((Mixin)match.mixin()).copy();
    MixinParams params = (MixinParams) match.params().eval(env);

    // Attempt to bind the arguments to this mixin's parameters. If the argument binding
    // failed, this is considered a resolution failure.
    GenericBlock bindings = matcher.bind(params);
    if (bindings == null) {
      return false;
    }

    // If the closure has been set on this mixin, use it.
    env = env.copy();
    Mixin original = (Mixin) mixin.original();
    ExecEnv closureEnv = original.closure();
    if (closureEnv != null) {
      env.append(closureEnv.frames());
    }

    // Push the argument bindings onto the closure stack and create the dual stack.
    // We can resolve variables against the closure + argument scope or the scope which
    // called the mixin.
    env.push(bindings);

    // Evaluate the guard conditions. If FALSE, bail out.
    Guard guard = mixin.guard();
    if (guard != null) {
      Node result = guard.eval(env);
      if (FALSE.equals(result)) {
        return true;
      }
    }

    // Limits the overall depth if the mixin call stack.
    LessContext ctx = env.context();
    if (ctx.mixinDepth() >= opts.recursionLimit()) {
      throw new LessException(mixinRecurse(call.path(), opts.recursionLimit()));
    }

    // Enter the mixin body and execute it.
    original.enter();
    ctx.enterMixin();

    env.push(mixin);

    LessException exception = null;
    try {
      Block block = mixin.block();
      expandMixins(env, block);

      // Wrap the final rules generated by this mixin call.
      if (opts.tracing()) {
        MixinCall actualCall = call.copy();
        actualCall.args(matcher.mixinArgs());
        block.prependNode(new MixinMarker(actualCall, original, true));
        block.appendNode(new MixinMarker(actualCall, original, false));
      }

      evaluateRules(env, block, call.important());
      exception = env.error();
      collector.appendBlock(block);

    } catch (LessException e) {
      exception = e;
    }

    // If any errors occur inside a mixin call, we want to show the actual
    // arguments passed to the call.
    if (exception != null) {
      MixinCall actualCall = call.copy();
      actualCall.args(matcher.mixinArgs());
      exception.push(actualCall);
      throw exception;
    }

    ctx.exitMixin();
    original.exit();
    return true;
  }

  /**
   * Executes a RULESET as a mixin.
   */
  private boolean executeRulesetMixin(ExecEnv env, Block collector, MixinMatcher matcher, MixinMatch match)
      throws LessException {
    MixinCall call = matcher.mixinCall();
    Ruleset ruleset = (Ruleset)match.mixin();

    // Ignore rulesets with guards that have not been evaluated.
    Ruleset original = (Ruleset)ruleset.original();
    if (original.selectors().guard() != null && !ruleset.evaluated()) {
      return true;
    }

    // Limits the overall depth if the mixin call stack.
    LessContext ctx = env.context();
    if (ctx.mixinDepth() >= opts.recursionLimit()) {
      throw new LessException(mixinRecurse(call.path(), opts.recursionLimit()));
    }

    ctx.enterMixin();
    Ruleset result = evaluateRuleset(env, ruleset, call.important());
    ctx.exitMixin();

    Block block = result.block();
    if (opts.tracing()) {
      block.prependNode(new MixinMarker(call, ruleset, true));
      block.appendNode(new MixinMarker(call, ruleset, false));
    }
    collector.appendBlock(block);
    return true;
  }

  /**
   * Evaluates a guard expression and returns true if the guard is null or evaluates to {@link True}.
   */
  private boolean evaluateGuard(ExecEnv env, Guard guard) throws LessException {
    if (guard != null) {
      Node result = guard.eval(env);
      if (!(result instanceof True)) {
        return false;
      }
    }
    return true;
  }

}


