package com.squarespace.v6.template.less;

import static com.squarespace.v6.template.less.model.NodeType.DIMENSION;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.List;

import org.testng.annotations.Test;

import com.squarespace.v6.template.less.core.LessTestBase;
import com.squarespace.v6.template.less.exec.ArgSpec;
import com.squarespace.v6.template.less.exec.ExecEnv;
import com.squarespace.v6.template.less.exec.Function;
import com.squarespace.v6.template.less.model.Node;
import com.squarespace.v6.template.less.model.Quoted;


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
