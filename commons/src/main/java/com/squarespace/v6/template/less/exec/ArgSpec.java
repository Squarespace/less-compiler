package com.squarespace.v6.template.less.exec;

import static com.squarespace.v6.template.less.ExecuteErrorType.ARG_COUNT;
import static com.squarespace.v6.template.less.ExecuteErrorType.INVALID_ARG;
import static com.squarespace.v6.template.less.core.ErrorUtils.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Dimension;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.NodeType;
import com.squarespace.v6.template.less.model.Unit;


/**
 * Defines a function's minimum number of arguments and their types.
 */
public class ArgSpec {

  private List<ArgValidator> validators;

  private int minArgs;
  
  private boolean variadic;
  
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
  
  public boolean validate(Function func, Node ... args) throws LessException {
    return validate(func, Arrays.asList(args));
  }
  
  public boolean validate(Function func, List<Node> args) throws LessException {
    int size = args.size();
    if (size < minArgs || (size > validators.size() && !variadic)) {
      throw new LessException(error(ARG_COUNT).name(func.name()).arg0(minArgs).arg1(size));
    }
    if (variadic) {
      size = validators.size();
    }
    for (int i = 0; i < size; i++) {
      try {
        validators.get(i).validate(i, args.get(i));
      } catch (LessException e) {
        System.out.println("Calling " + func.name());
        throw e;
      }
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
    throw new RuntimeException("Unknown type ch: '" + ch + "'");
  }
 
  static class ArgTypeValidator extends ArgValidator {
    
    private NodeType type;
    
    public ArgTypeValidator(NodeType type) {
      this.type = type;
    }
    
    @Override
    public void validate(int index, Node arg) throws LessException {
      if (!arg.is(type)) {
        throw new LessException(error(INVALID_ARG).arg0(index).type(type));
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
        throw new LessException(error(INVALID_ARG).arg0(index).type(NodeType.DIMENSION));
      }
      Dimension dim = (Dimension)arg;
      if (dim.unit() == null) {
        return;
      }
      throw new LessException (error(INVALID_ARG).arg0(index).type("a unit-less number"));
    }
  };
  
  private static ArgValidator _PERCENTAGE = new ArgValidator() {
    @Override
    public void validate(int index, Node arg) throws LessException {
      if (!arg.is(NodeType.DIMENSION)) {
        throw new LessException(error(INVALID_ARG).arg0(index).type(NodeType.DIMENSION));
      }
      Dimension dim = (Dimension)arg;
      if (dim.unit() == null || dim.unit() == Unit.PERCENTAGE) {
        return;
      }
      throw new LessException(error(INVALID_ARG).arg0(index).type("a unit-less number or a percentage"));
    }
  };
  
}
