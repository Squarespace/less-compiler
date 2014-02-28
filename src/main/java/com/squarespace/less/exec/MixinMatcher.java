package com.squarespace.less.exec;

import static com.squarespace.less.core.ExecuteErrorMaker.argNamedNotFound;
import static com.squarespace.less.core.ExecuteErrorMaker.argTooMany;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.squarespace.less.Context;
import com.squarespace.less.LessException;
import com.squarespace.less.core.LessInternalException;
import com.squarespace.less.model.Argument;
import com.squarespace.less.model.Block;
import com.squarespace.less.model.Definition;
import com.squarespace.less.model.Expression;
import com.squarespace.less.model.GenericBlock;
import com.squarespace.less.model.MixinCall;
import com.squarespace.less.model.MixinCallArgs;
import com.squarespace.less.model.MixinParams;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Parameter;


/**
 * Carries out argument-to-parameter pattern matching and binding for MIXIN
 * resolution and execution.
 */
public class MixinMatcher {

  private final ExecEnv callEnv;
  
  private final MixinCall mixinCall;
  
  private final MixinCallArgs mixinArgs;
  
  public MixinMatcher(ExecEnv callEnv, MixinCall call) throws LessException {
    this.callEnv = callEnv;
    this.mixinCall = call;
    MixinCallArgs args = call.args();
    this.mixinArgs = (MixinCallArgs) (args == null ? args : args.eval(callEnv));
  }
  
  public MixinCall mixinCall() {
    return mixinCall;
  }
  
  public MixinCallArgs mixinArgs() {
    return mixinArgs;
  }
  
  public ExecEnv callEnv() {
    return callEnv;
  }
  
  /**
   * Attempts to bind the mixin arguments to the mixin parameters. This happens in the following
   * discrete steps:
   * 
   *  1. Bind default values for named parameters and create the expression to capture variadic
   *     arguments, if any.
   *  2. Bind named arguments and track which named parameters have been bound.
   *  3. Bind positional argument values to remaining named parameters, or collect the argument's
   *     value in the variadic expression, or skip them if they were just used for pattern matching.
   *  4. Build the final list of bindings and add the special "@arguments" variable.
   */
  public GenericBlock bind(MixinParams mixinParams) throws LessException {
    if (mixinParams.needsEval()) {
      throw new LessInternalException("Serious error: params must already be evaluated!");
    }
    
    List<Parameter> params = mixinParams.params();
    List<Argument> args = mixinArgs == null ? null : mixinArgs.args();
    int paramSize = params.size();
    int argSize = args == null ? 0 : args.size();

    Map<String, Node> boundValues = new LinkedHashMap<>();
    Queue<String> names = new ArrayDeque<>();
    
    String variadicName = null;
    Expression variadic = null;
    
    // Bind parameter default values, collect names, and prepare variadic expression, if any.
    for (int i = 0; i < paramSize; i++) {
      Parameter param = params.get(i);
      String paramName = param.name();
      if (param.variadic()) {
        variadicName = paramName;
        variadic = new Expression();

      } else if (paramName != null) {
        names.add(paramName);
        Node value = param.value();
        if (value != null) {
          boundValues.put(paramName, value);
        }
      }
    }

    // Bind all named arguments.
    for (int i = 0; i < argSize; i++) {
      Argument arg = args.get(i);
      String argName = arg.name();
      if (argName == null) {
        continue;
      }
      if (!names.contains(argName)) {
        LessException exc = new LessException(argNamedNotFound(argName));
        exc.push(mixinCall);
        throw exc;
      }
      boundValues.put(argName, arg.value());
      names.remove(argName);
    }

    // Bind all remaining positional arguments.
    for (int i = 0; i < argSize; i++) {
      Argument arg = args.get(i);
      String argName = arg.name();
      if (argName != null) {
        continue;
      }
      
      // Have a positional parameter?  Check for variadic or value pattern match
      boolean haveParams = i < paramSize;
      if (haveParams) {
        Parameter param = params.get(i);
        if (param.variadic()) {
          variadic.add(arg.value());
          continue;
          
        } else if (param.name() == null) {
          // Pattern match.
          continue;
        }
      }

      // Positional, assign to one of the remaining named arguments if any.
      if (!names.isEmpty()) {
        argName = names.poll();
        boundValues.put(argName, arg.value());

      } else if (variadic != null) {
        variadic.add(arg.value());

      } else {
        // No names left and no variadic exists to collect overflow
        // We should never reach this point since patternMatch() would have also failed.
        throw new LessException(argTooMany());
      }
    }
    
    // Build the final bindings block.
    Expression arguments = new Expression();
    Block bindings = new Block(boundValues.size());
    for (Map.Entry<String, Node> entry : boundValues.entrySet()) {
      Node value = entry.getValue();
      bindings.appendNode(new Definition(entry.getKey(), value));
      arguments.add(value);
    }
    if (variadicName != null) {
      bindings.appendNode(new Definition(variadicName, variadic));
    }
    if (variadic != null) {
      for (Node value : variadic.values()) {
        arguments.add(value);
      }
    }
    bindings.appendNode(new Definition("@arguments", arguments));
    return new GenericBlock(bindings);
  }
  
  /**
   * Determine if the arguments match the parameter's pattern.
   */
  public boolean patternMatch(MixinParams mixinParams) throws LessException {
    List<Parameter> params = mixinParams.params();
    List<Argument> args = mixinArgs == null ? null : mixinArgs.args();
    int paramSize = params.size();
    int argSize = args == null ? 0 : args.size();
    
    if (argSize < mixinParams.required()) {
      return false;
    }
    if (!mixinParams.variadic() && argSize > params.size()) {
      return false;
    }

    // Check if args are compatible and match the parameter pattern fully.
    int size = Math.min(argSize, paramSize);
    for (int i = 0; i < size; i++) {
      Argument arg = args.get(i);
      Parameter param = params.get(i);
      String paramName = param.name();
      if (paramName == null && !param.variadic() && !valueEquals(arg, param)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * Check if the argument's value is equal to the parameter's value. It first
   * tries the Node.equals() method, and if that fails it falls back to comparing
   * the rendered output of each node.
   */
  private boolean valueEquals(Argument arg, Parameter param) throws LessException {
    Node val1 = arg.value();
    Node val2 = param.value();
    if (!val1.equals(val2)) {
      // If Node.equals() fails, try compare the rendered output.
      Context ctx = callEnv.context();
      String v0 = ctx.render(val1);
      String v1 = ctx.render(val2);
      if (!v0.equals(v1)) {
        return false;
      }
    }
    return true;
  }

}
