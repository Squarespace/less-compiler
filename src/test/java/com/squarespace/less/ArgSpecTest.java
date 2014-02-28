package com.squarespace.less;

import static com.squarespace.less.model.NodeType.DIMENSION;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.less.Context;
import com.squarespace.less.LessException;
import com.squarespace.less.core.LessTestBase;
import com.squarespace.less.exec.ArgSpec;
import com.squarespace.less.exec.ExecEnv;
import com.squarespace.less.exec.Function;
import com.squarespace.less.model.Node;
import com.squarespace.less.model.Quoted;


public class ArgSpecTest extends LessTestBase {

  @Test
  public void testValidate() throws LessException {
    ArgSpec spec = argspec("d");
    valid(spec, dim(1));
    
    invalid(spec);
    invalid(spec, anon("x"));
    invalid(spec, dim(1), anon("x"));
    invalid(spec, anon("x"), dim(1));

    spec = argspec("d:d");
    valid(spec, dim(1));
    valid(spec, dim(2), dim(3));

    invalid(spec);
    invalid(spec, dim(1), anon("x"));
    invalid(spec, dim(1), dim(2), dim(3));
    
    spec = argspec(".");
    valid(spec);
    valid(spec, dim(1), dim(2), (dim(3)));
    
    spec = argspec("*");
    valid(spec, dim(1));
    valid(spec, anon("foo"));
    valid(spec, quoted('"', false, "foo"));

    Quoted str = quoted('"', false, "a");
    spec = argspec("ss");
    valid(spec, str, str);
    invalid(spec, str);
    invalid(spec, str, str, str);
    
    try {
      argspec(2, DIMENSION);
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException e) {
    }

  }

  private ExecEnv env() {
    return new Context().newEnv();
  }
  
  private void valid(ArgSpec spec, Node ... nodes) throws LessException {
    assertTrue(spec.validate(env(), dummy(spec), nodes));
  }
  
  private void invalid(ArgSpec spec, Node ... nodes) throws LessException {
    try {
      spec.validate(env(), dummy(spec), nodes);
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
