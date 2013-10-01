package com.squarespace.v6.template.less.exec;

import static com.squarespace.v6.template.less.ExecuteErrorType.MIXIN_RECURSE;
import static com.squarespace.v6.template.less.ExecuteErrorType.MIXIN_UNDEFINED;
import static com.squarespace.v6.template.less.core.Constants.FALSE;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.squarespace.v6.template.less.Context;
import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.Options;
import com.squarespace.v6.template.less.core.FlexList;
import com.squarespace.v6.template.less.model.Block;
import com.squarespace.v6.template.less.model.BlockDirective;
import com.squarespace.v6.template.less.model.Definition;
import com.squarespace.v6.template.less.model.Directive;
import com.squarespace.v6.template.less.model.Features;
import com.squarespace.v6.template.less.model.GenericBlock;
import com.squarespace.v6.template.less.model.Guard;
import com.squarespace.v6.template.less.model.Import;
import com.squarespace.v6.template.less.model.Media;
import com.squarespace.v6.template.less.model.Mixin;
import com.squarespace.v6.template.less.model.MixinCall;
import com.squarespace.v6.template.less.model.MixinParams;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Rule;
import com.squarespace.v6.template.less.model.Ruleset;
import com.squarespace.v6.template.less.model.Stylesheet;


/**
 * Given a parse tree, executes it.
 */
public class LessEngine {

  private static final Pattern IMPORT_EXT = Pattern.compile(".*(\\.[a-z]*$)|([\\?;].*)$");
  
  private static final Pattern IMPORT_CSS = Pattern.compile(".*css([\\?;].*)?$");
  
  public LessEngine() {
  }
  
  /**
   * Evaluate and render a LESS stylesheet, using the given context.
   */
  public String render(Stylesheet sheet, Context context) throws LessException {
    sheet = evaluateStylesheet(context.newEnv(), sheet);
    return new LessRenderer().render(context, sheet);
  }

  public Stylesheet expand(Stylesheet sheet, Context context) throws LessException {
    return evaluateStylesheet(context.newEnv(), sheet);
  }
  
  
  /**
   * Evaluate a BLOCK_DIRECTIVE node.
   */
  private BlockDirective evaluateBlockDirective(ExecEnv env, BlockDirective input) throws LessException {
    BlockDirective directive = input.copy();
    env.push(directive);

    Block block = directive.block();
    expandImports(env, block);
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
    env.push(media);
    
    Block block = media.block();
    expandImports(env, block);
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
    expandImports(env, block);
    expandMixins(env, block);
    evaluateRules(env, block, forceImportant);
    
    original.exit();
    env.pop();
    return ruleset;
  }

  /**
   * Evaluate a STYLESHEET node.
   */
  private Stylesheet evaluateStylesheet(ExecEnv env, Stylesheet original) throws LessException {
    Stylesheet stylesheet = original.copy();
    env.push(stylesheet);

    Block block = stylesheet.block();
    expandImports(env, block);
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
    for (int i = 0; i < rules.size(); i++) {
      Node node = rules.get(i);
      
      switch (node.type()) {

        case BLOCK_DIRECTIVE:
          node = evaluateBlockDirective(env, (BlockDirective)node);
          break;
        
        case DEFINITION:
          Definition def = (Definition)node;
          node = new Definition(def.name(), def.dereference(env));
          break;
          
        case DIRECTIVE:
          Directive directive = (Directive)node;
          if (directive.name().equals("@charset") && block.charset() == null) {
            block.charset(directive);
          }
          break;

        case MEDIA:
          node = evaluateMedia(env, (Media)node);
          break;
          
        case MIXIN:
          // Register the closure on the original MIXIN.
          Mixin mixin = (Mixin) ((Mixin)node).original();
          mixin.closure(env);
          break;
        
        case MIXIN_CALL:
          throw new RuntimeException("Serious error: all mixin calls should already have been evaluated.");
          
        case STYLESHEET:
          node = evaluateStylesheet(env, (Stylesheet)node);
          break;
          
        case RULESET:
          node = evaluateRuleset(env, (Ruleset)node, forceImportant);
          break;
          
        case RULE:
          Rule rule = (Rule) node;
          if (forceImportant && !rule.important()) {
            node = new Rule(rule.property(), rule.value().eval(env), forceImportant);
          } else {
            node = rule.eval(env);
          }
          break;
          
        default:
          node = node.eval(env);
          break;
      }
      rules.set(i, node);
    }
  }
  
  private void expandImports(ExecEnv env, Block block) throws LessException {
    FlexList<Node> rules = block.rules();
    // Use of rules.size() intentional since the list size can change during iteration.
    for (int i = 0; i < rules.size(); i++) {
      Node node = rules.get(i);
      switch (node.type()) {
        
        case IMPORT:
          Block importResult = executeImport(env, (Import)node);
          if (importResult != null) {
            FlexList<Node> other = importResult.rules();
            rules.splice(i, 1, other);
            i += other.size() - 1;
            block.resetCache();

          } else {
            // Skip, leave the IMPORT in place since it will be emitted as-is.
          }
          
        default:
          break;
      }
    }
  }
  
  private Block executeImport(ExecEnv env, Import imp) throws LessException {
    String path = imp.renderPath(env);
    Matcher matcher = IMPORT_EXT.matcher(path);
    if (!matcher.matches()) {
      path += ".less";
    }
    matcher = IMPORT_CSS.matcher(path);
    if (matcher.matches()) {
      return null;
    }
    
    Context ctx = env.context();
    Stylesheet stylesheet = ctx.parseImport(path, imp.rootPath(), imp.once());
    if (stylesheet == null) {
      return new Block(0);
    }
    expandImports(env, stylesheet.block());

    Features features = imp.features();
    if (features == null || features.isEmpty()) {
      return stylesheet.block();
    }
    
    // If the IMPORT has media features, wrap the block in a MEDIA.
    features = (Features) features.eval(env);
    Block block = new Block();
    block.append(new Media(features, stylesheet.block()));
    return block;
  }
  
  /**
   * Iterate over all rules in this block and execute all of the MIXIN_CALL rules found.
   * Each successful call will produce multiple rules. We replace the call with
   * the rules it produced.
   */
  private void expandMixins(ExecEnv env, Block block) throws LessException {
    FlexList<Node> rules = block.rules();
    // Use of rules.size() intentional since the list size can change during iteration.
    for (int i = 0; i < rules.size(); i++) {
      Node node = rules.get(i);
      switch (node.type()) {
        
        case MIXIN_CALL:
          Block mixinResult = executeMixinCall(env, (MixinCall)node);
          FlexList<Node> other = mixinResult.rules();
          rules.splice(i, 1, other);
          i += other.size() - 1;
          block.resetCache();
          break;

        default:
          break;
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
    MixinResolver resolver = env.context().mixinResolver();
    resolver.reset(matcher);
    env.resolveMixins(resolver);
    List<MixinMatch> matches = resolver.matches();
    if (matches.isEmpty()) {
      throw new LessException(error(MIXIN_UNDEFINED).arg0(env.context().render(call.selector())));
    }

    Block results = new Block();
    int calls = 0;
    for (MixinMatch match : matches) {
      Node node = match.mixin();
      switch (node.type()) {
        
        case MIXIN:
          if (executeMixin(env, results, matcher, match)) {
            calls++;
          }
          break;
          
        case RULESET:
          if (executeRulesetMixin(env, results, matcher, match)) {
            calls++;
          }
          break;
          
        default:
          break;
      }
    }
    
    if (calls == 0) {
      throw new LessException(error(MIXIN_UNDEFINED).arg0(env.context().render(call.selector())));
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
    
    // Caps the number of recursions through this mixin.
    Options opts = env.context().options();
    if (original.entryCount() >= opts.recursionLimit()) {
      throw new LessException(error(MIXIN_RECURSE).name(call.path()).arg0(opts.recursionLimit()));
    }

    // Enter the mixin body and execute it.
    original.enter();
    env.push(mixin);

    Block block = mixin.block();
    expandMixins(env, block);
    evaluateRules(env, block, call.important());
    collector.append(block);
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
    Ruleset result = evaluateRuleset(env, ruleset, call.important());
    collector.append(result.block());
    return true;
  }
  
}


