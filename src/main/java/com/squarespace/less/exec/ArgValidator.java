package com.squarespace.less.exec;

import com.squarespace.less.LessException;
import com.squarespace.less.model.Node;


public abstract class ArgValidator {

  public abstract void validate(int index, Node arg) throws LessException;

}
