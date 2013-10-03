package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.NodeType.DIMENSION;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.exec.ArgSpec;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.model.Node;


public class ArgSpecTest extends LessTestBase {

  @Test
  public void testValidate() throws LessException {
    ArgSpec spec = argspec(DIMENSION);
    valid(spec, dim(1));
    
    invalid(spec);
    invalid(spec, anon("x"));
    invalid(spec, dim(1), anon("x"));
    invalid(spec, anon("x"), dim(1));

    spec = argspec(1, DIMENSION, DIMENSION);
    valid(spec, dim(1));
    valid(spec, dim(2), dim(3));

    invalid(spec);
    invalid(spec, dim(1), anon("x"));
    invalid(spec, dim(1), dim(2), dim(3));
    
    try {
      argspec(2, DIMENSION);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

  }
  
  private void valid(ArgSpec spec, Node ... nodes) throws LessException {
    assertTrue(spec.validate(dummy(spec), nodes));
  }
  
  private void invalid(ArgSpec spec, Node ... nodes) throws LessException {
    try {
      spec.validate(dummy(spec), nodes);
      fail("Expected LessException for " + spec);
    } catch (LessException e) {
    }
  }

  private Function dummy(ArgSpec spec) {
    return new Function("dummy", spec) {
      @Override
      public Node invoke(ExecEnv env, List<Node> args) throws LessException {
        return null;
      }
    };
  }
  
}
