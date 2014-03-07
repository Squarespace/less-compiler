package com.squarespace.less.exec;

import static com.squarespace.less.core.ExecuteErrorMaker.argCount;
import static com.squarespace.less.core.ExecuteErrorMaker.invalidArg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squarespace.less.ErrorInfo;
import com.squarespace.less.LessException;
import com.squarespace.less.Options;
import com.squarespace.less.model.Dimension;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.NodeType;
import com.squarespace.less.model.Unit;


/**
 * Defines a function's signature.
 */
public class ArgSpec {

  private final List<ArgValidator> validators;

  private final int minArgs;

  private final boolean variadic;

  public ArgSpec(NodeType ... types) {
    this(types.length, types);
  }

  public ArgSpec(int minArgs, NodeType ... types) {
    this(minArgs, build(types));
 }

  public ArgSpec(ArgValidator ... validators) {
    this(validators.length, validators);
  }

  public ArgSpec(int minArgs, ArgValidator ... validators) {
    this(minArgs, Arrays.asList(validators), false);
  }

  public ArgSpec(int minArgs, List<ArgValidator> validators, boolean variadic) {
    if (!variadic && validators.size() < minArgs) {
      throw new IllegalArgumentException("minArgs cannot be < zero or exceed types.length");
    }
    this.minArgs = minArgs;
    this.validators = validators;
    this.variadic = variadic;
  }

  private static ArgValidator[] build(NodeType ... types) {
    return build(Arrays.asList(types));
  }

  private static ArgValidator[] build(List<NodeType> types) {
    int size = types.size();
    ArgValidator[] validators = new ArgValidator[size];
    for (int i = 0; i < size; i++) {
      validators[i] = new ArgTypeValidator(types.get(i));
    }
    return validators;
  }

  public boolean validate(ExecEnv env, Function func, Node ... args) throws LessException {
    return validate(env, func, Arrays.asList(args));
  }

  public boolean validate(ExecEnv env, Function func, List<Node> args) throws LessException {
    int size = args.size();
    if (size < minArgs) {
      throw new LessException(argCount(func.name(), minArgs, size));

    } else if (size > validators.size() && !variadic) {
      Options opts = env.context().options();
      ErrorInfo info = argCount(func.name(), minArgs, size);
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

  public static ArgSpec parseSpec(String raw) {
    List<ArgValidator> validators = new ArrayList<>();
    int minArgs = -1;
    int size = raw.length();
    boolean variadic = false;
    for (int i = 0; i < size; i++) {
      char ch = raw.charAt(i);
      switch (ch) {
        case 'p':
          validators.add(_PERCENTAGE);
          break;

        case 'n':
          validators.add(_NUMBER);
          break;

        case '*':
          validators.add(_ANY);
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

  private static NodeType fromChar(char ch) {
    switch (ch) {
      case 'c':
        return NodeType.COLOR;
      case 'd':
        return NodeType.DIMENSION;
      case 'k':
        return NodeType.KEYWORD;
      case 's':
        return NodeType.QUOTED;
    }
    throw new IllegalArgumentException("Unknown type ch: '" + ch + "'");
  }

  static class ArgTypeValidator extends ArgValidator {

    private final NodeType type;

    public ArgTypeValidator(NodeType type) {
      this.type = type;
    }

    @Override
    public void validate(int index, Node arg) throws LessException {
      if (!arg.is(type)) {
        throw new LessException(invalidArg(index + 1, type, arg.type()));
      }
    }

  }

  private static ArgValidator _ANY = new ArgValidator() {
    @Override
    public void validate(int index, Node arg) throws LessException {
      // any node type is valid.
    };
  };

  private static ArgValidator _NUMBER = new ArgValidator() {
    @Override
    public void validate(int index, Node arg) throws LessException {
      if (!arg.is(NodeType.DIMENSION)) {
        throw new LessException(invalidArg(index + 1, NodeType.DIMENSION, arg.type()));
      }
      Dimension dim = (Dimension)arg;
      if (dim.unit() == null) {
        return;
      }
      throw new LessException(invalidArg(index, "a unit-less number", arg.type()));
    }
  };

  private static ArgValidator _PERCENTAGE = new ArgValidator() {
    @Override
    public void validate(int index, Node arg) throws LessException {
      if (!arg.is(NodeType.DIMENSION)) {
        throw new LessException(invalidArg(index + 1, NodeType.DIMENSION, arg.type()));
      }
      Dimension dim = (Dimension)arg;
      if (dim.unit() == null || dim.unit() == Unit.PERCENTAGE) {
        return;
      }
      throw new LessException(invalidArg(index + 1, "a unit-less number or a percentage", arg.type()));
    }
  };

}
