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

import static com.squarespace.less.core.ExecuteErrorMaker.argCount;
import static com.squarespace.less.core.ExecuteErrorMaker.invalidArg;
import static com.squarespace.less.core.ExecuteErrorMaker.invalidArgExt;
import static com.squarespace.less.model.NodeType.COLOR;
import static com.squarespace.less.model.NodeType.DIMENSION;
import static com.squarespace.less.model.NodeType.KEYWORD;
import static com.squarespace.less.model.NodeType.QUOTED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squarespace.less.LessErrorInfo;
import com.squarespace.less.LessException;
import com.squarespace.less.LessOptions;
import com.squarespace.less.model.Argument;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.FunctionCall;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Unit;


/**
 * Defines a {@link FunctionCall}'s signature.
 */
public class ArgSpec {

  /**
   * Validate the arguments before invoking a function.
   */
  private final List<ArgValidator> validators;

  /**
   * Minimum number of arguments required.
   */
  private final int minArgs;

  /**
   * Whether the function accepts a variable number of arguments.
   */
  private final boolean variadic;

  /**
   * Constructs an instance which ensures that the arguments are of the
   * given {@code types}.
   */
  public ArgSpec(NodeType ... types) {
    this(types.length, types);
  }

  /**
   * Like {@link ArgSpec#ArgSpec(NodeType...)} but ensures that at least
   * {@code minArgs} are passed.
   */
  public ArgSpec(int minArgs, NodeType ... types) {
    this(minArgs, build(types));
 }

  /**
   * Constructs an instance which ensures that the arguments are valid,
   * using the given {@code validators}. Each {@link ArgValidator}
   * validates the argument in the corresponding position.
   */
  public ArgSpec(ArgValidator ... validators) {
    this(validators.length, validators);
  }

  /**
   * Like {@link ArgSpec#ArgSpec(ArgValidator...)} but ensures that at least
   * {@code minArgs} are passed.
   */
  public ArgSpec(int minArgs, ArgValidator ... validators) {
    this(minArgs, Arrays.asList(validators), false);
  }

  /**
   * Like {@link ArgSpec#ArgSpec(int, ArgValidator...)} with variable argument
   * support.
   */
  public ArgSpec(int minArgs, List<ArgValidator> validators, boolean variadic) {
    if (!variadic && validators.size() < minArgs) {
      throw new IllegalArgumentException("minArgs cannot be < zero or exceed types.length");
    }
    this.minArgs = minArgs;
    this.validators = validators;
    this.variadic = variadic;
  }

  /**
   * Builds validators which ensure that the arguments are of the given {@code types}.
   */
  private static ArgValidator[] build(NodeType ... types) {
    return build(Arrays.asList(types));
  }

  /**
   * Builds validators which ensure that the arguments are of the given {@code types}.
   */
  private static ArgValidator[] build(List<NodeType> types) {
    int size = types.size();
    ArgValidator[] validators = new ArgValidator[size];
    for (int i = 0; i < size; i++) {
      validators[i] = new ArgTypeValidator(types.get(i));
    }
    return validators;
  }

  /**
   * Validates the arguments.
   */
  public boolean validate(ExecEnv env, Function func, Node ... args) throws LessException {
    return validate(env, func, Arrays.asList(args));
  }

  /**
   * Validates the arguments.
   */
  public boolean validate(ExecEnv env, Function func, List<Node> args) throws LessException {
    int size = args.size();
    if (size < minArgs) {
      throw new LessException(argCount(func.name(), minArgs, size));

    } else if (size > validators.size() && !variadic) {
      LessOptions opts = env.context().options();
      LessErrorInfo info = argCount(func.name(), minArgs, size);
      if (opts.strict()) {
        throw new LessException(info);
      }
      if (!opts.hideWarnings()) {
        // Ignore the additional arguments.
        env.addWarning(info.getMessage() + ".. ignoring additional args");
      }
      size = validators.size();
    }
    if (variadic) {
      size = validators.size();
    }
    for (int i = 0; i < size; i++) {
      validators.get(i).validate(i, args.get(i));
    }
    return true;
  }

  /**
   * Parses the specification string into an {@link ArgSpec} instance.
   */
  public static ArgSpec fromString(String raw) {
    List<ArgValidator> validators = new ArrayList<>();
    int minArgs = -1;
    int size = raw.length();
    boolean variadic = false;
    for (int i = 0; i < size; i++) {
      char ch = raw.charAt(i);
      switch (ch) {
        case 'p':
          validators.add(ARG_PERCENTAGE);
          break;

        case 'n':
          validators.add(ARG_NUMBER);
          break;

        case '*':
          validators.add(ARG_ANY);
          break;

        case ':':
          minArgs = i;
          break;

        case '.':
          variadic = true;
          minArgs = i;
          break;

        default:
          validators.add(new ArgTypeValidator(fromChar(ch)));
          break;
      }
      if (variadic) {
        break;
      }
    }
    if (minArgs == -1) {
      minArgs = size;
    }
    return new ArgSpec(minArgs, validators, variadic);
  }

  /**
   * Maps the specification character to a node type.
   */
  private static NodeType fromChar(char ch) {
    switch (ch) {
      case 'c':
        return COLOR;
      case 'd':
        return DIMENSION;
      case 'k':
        return KEYWORD;
      case 's':
        return QUOTED;
      default:
        break;
    }
    throw new IllegalArgumentException("Unknown type ch: '" + ch + "'");
  }

  /**
   * Validates an {@link Argument} by its {@link Node#type()}.
   */
  static class ArgTypeValidator extends ArgValidator {

    private final NodeType type;

    ArgTypeValidator(NodeType type) {
      this.type = type;
    }

    @Override
    public void validate(int index, Node arg) throws LessException {
      if (arg.type() != type) {
        throw new LessException(invalidArgExt(index + 1, type, arg.type(), arg));
      }
    }

  }

  /**
   * Validator which accepts any node type.
   */
  private static final ArgValidator ARG_ANY = new ArgValidator() {
    @Override
    public void validate(int index, Node arg) throws LessException {
      // any node type is valid.
    };
  };

  /**
   * Validator which only accepts a unit-less number.
   */
  private static final ArgValidator ARG_NUMBER = new ArgValidator() {
    @Override
    public void validate(int index, Node arg) throws LessException {
      if (arg.type() != DIMENSION) {
        throw new LessException(invalidArg(index + 1, DIMENSION, arg.type()));
      }
      Dimension dim = (Dimension)arg;
      if (dim.unit() == null) {
        return;
      }
      throw new LessException(invalidArg(index, "a unit-less number", arg.type()));
    }
  };

  /**
   * Validator which only accepts numbers in percentage units.
   */
  private static final ArgValidator ARG_PERCENTAGE = new ArgValidator() {
    @Override
    public void validate(int index, Node arg) throws LessException {
      if (arg.type() != DIMENSION) {
        throw new LessException(invalidArg(index + 1, DIMENSION, arg.type()));
      }
      Dimension dim = (Dimension)arg;
      if (dim.unit() == null || dim.unit() == Unit.PERCENTAGE) {
        return;
      }
      throw new LessException(invalidArg(index + 1, "a unit-less number or a percentage", arg.type()));
    }
  };

}
