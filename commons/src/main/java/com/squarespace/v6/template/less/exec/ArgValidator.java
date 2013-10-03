package com.squarespace.v6.template.less.exec;

import com.squarespace.v6.template.less.LessException;
import com.squarespace.v6.template.less.model.Node;


public abstract class ArgValidator {

  public abstract void validate(int index, Node arg) throws LessException; 
  
}
